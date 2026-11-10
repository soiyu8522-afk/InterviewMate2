package interviewmate;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

/** 면접메이트 – 실행 시작점, 화면 전환, 앱 상태 */
public class App {
    public static final int W = 362, H = 784;   // 목업 이미지(724x1568)의 절반 크기 (모든 좌표의 기준)
    /** 화면 배율. 노트북 화면 높이에 맞춰 자동으로 정해지고, Ctrl + / Ctrl - 로 바꿀 수 있음 */
    public static double S = 1;
    public static int px(double v) { return (int) Math.round(v * S); }

    private static JFrame frame;
    private static JPanel root;
    private static final Map<String, Canvas> screens = new HashMap<>();
    private static final Map<String, Supplier<Canvas>> factory = new LinkedHashMap<>();
    private static final Deque<String> history = new ArrayDeque<>();
    private static String current;
    private static Toast toast;

    // ===== 앱 상태 =====
    public static String userName = "지원";
    public static int streak = 7, bestStreak = 12, level = 4, xp = 320, points = 240;
    public static int interviewer = 1;          // Data.TYPES 인덱스
    public static int job = 0, questionCount = 5;
    public static boolean realtime = false;     // false = 녹화 후 피드백
    public static boolean tts = true, saveVideo = true, subtitle = false;
    public static int surpriseFreq = 1;         // 0 없음, 1 가끔, 2 자주
    public static boolean executiveUnlocked = false;
    public static int bossTries = 2, bossBest = 62;
    public static boolean bossCleared = false;
    public static int weeklyDays = 4;
    public static boolean[] weekDone = {true, true, true, false, false, false, false};
    /** 데일리 퀘스트: 진행도, 목표, 보상, 받았는지 */
    public static final Data.Quest[] quests = Data.defaultQuests();
    /** 면접에 쓸 맞춤 질문 (채용공고·자소서에서 넘어온 것). null 이면 성향별 기본 질문 */
    public static List<String> customQuestions = null;
    public static boolean bossMode = false;
    public static final List<Data.Session> sessions = Data.defaultSessions();
    public static Data.Session lastSession = sessions.get(0);
    public static String resumeText = Data.RESUME_DEFAULT;
    public static String postingText = Data.POSTING_DEFAULT;

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        SwingUtilities.invokeLater(App::start);
    }

    /** 작업 표시줄을 뺀 화면 높이에 창이 딱 들어가는 배율 */
    private static double fitScale() {
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        double s = (screen.height - 50) / (double) H;      // 50 = 창 제목 표시줄 여유
        return Math.max(0.6, Math.min(1.6, s));
    }

    private static void start() {
        S = fitScale();
        register();
        frame = new JFrame("면접메이트");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        root = new JPanel(new BorderLayout());
        root.setPreferredSize(new Dimension(px(W), px(H)));
        frame.setContentPane(root);
        toast = new Toast();
        frame.setGlassPane(toast);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        go("home");
        installZoomKeys();
        frame.setVisible(true);
    }

    /** Ctrl + (크게), Ctrl - (작게), Ctrl 0 (화면에 맞춤) */
    private static void installZoomKeys() {
        JRootPane rp = frame.getRootPane();
        InputMap im = rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = rp.getActionMap();
        int ctrl = java.awt.event.InputEvent.CTRL_DOWN_MASK;
        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_EQUALS, ctrl), "zoomIn");
        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ADD, ctrl), "zoomIn");
        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_MINUS, ctrl), "zoomOut");
        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SUBTRACT, ctrl), "zoomOut");
        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_0, ctrl), "zoomFit");
        am.put("zoomIn", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { rescale(S + 0.1); } });
        am.put("zoomOut", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { rescale(S - 0.1); } });
        am.put("zoomFit", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { rescale(fitScale()); } });
    }

    /** 배율을 바꾸고 화면을 새로 만듦 (면접 중에는 바꾸지 않음) */
    public static void rescale(double s) {
        if ("interview".equals(current)) { toast("면접 중에는 크기를 바꿀 수 없어요"); return; }
        S = Math.max(0.5, Math.min(2.0, Math.round(s * 10) / 10.0));
        screens.clear();
        root.setPreferredSize(new Dimension(px(W), px(H)));
        frame.pack();
        show(current);
        toast("화면 크기 " + Math.round(S * 100) + "%");
    }

    public static void register() {
        if (!factory.isEmpty()) return;
        factory.put("home", HomeScreen::new);
        factory.put("select", SelectScreen::new);
        factory.put("feedback", FeedbackModeScreen::new);
        factory.put("interview", InterviewScreen::new);
        factory.put("report", ReportScreen::new);
        factory.put("quest", QuestScreen::new);
        factory.put("thoughts", ThoughtsScreen::new);
        factory.put("boss", BossScreen::new);
        factory.put("posting", PostingScreen::new);
        factory.put("resume", ResumeScreen::new);
        factory.put("expected", ExpectedScreen::new);
        factory.put("company", CompanyScreen::new);
        factory.put("review", ReviewScreen::new);
    }

    /** 화면 이동 */
    public static void go(String name) {
        if (current != null && !current.equals(name)) history.push(current);
        show(name);
    }
    /** 탭 이동 (뒤로가기 기록을 쌓지 않음) */
    public static void tab(String name) { history.clear(); show(name); }
    public static void back() { show(history.isEmpty() ? "home" : history.pop()); }

    private static void show(String name) {
        Canvas s = screens.computeIfAbsent(name, k -> factory.get(k).get());
        current = name;
        // 카메라·마이크는 피드백 설정 화면과 면접 화면에서만 켜 둠
        if (name.equals("feedback") || name.equals("interview")) Media.start(); else Media.stop();
        root.removeAll();
        root.add(s, BorderLayout.CENTER);
        s.onShow();
        root.revalidate();
        root.repaint();
    }
    public static Canvas screen(String name) { return screens.computeIfAbsent(name, k -> factory.get(k).get()); }
    public static JFrame frame() { return frame; }

    public static void toast(String msg) { if (toast != null) toast.show(msg); }

    /** XP 추가 + 레벨업 */
    public static void addXp(int amount) {
        xp += amount;
        while (xp >= 500) { xp -= 500; level++; toast("레벨 업! Lv." + level + " 달성"); }
    }

    /** 퀘스트 진행도 올리기 */
    public static void progress(String id, int amount) {
        for (Data.Quest q : quests) if (q.id.equals(id)) q.now = Math.min(q.goal, q.now + amount);
    }
    public static Data.Quest quest(String id) {
        for (Data.Quest q : quests) if (q.id.equals(id)) return q;
        return null;
    }

    // ===== 공통 하단 탭바 =====
    public static void navBar(Graphics2D g, Canvas c, int active) {
        int y = H - 84;
        G.rr(g, 0, y, W, 84, 0, G.WHITE);
        G.line(g, 0, y, W, y, G.TRACK, 1);
        String[] names = {"홈", "면접", "퀘스트", "리포트"};
        String[] dest = {"home", "select", "quest", "report"};
        double cell = W / 4.0;
        for (int i = 0; i < 4; i++) {
            Color col = i == active ? (i == 2 ? G.ORANGE : G.BLUE) : G.SLATE;
            double cx = cell * i + cell / 2;
            double iy = y + 16;
            switch (i) {
                case 0: G.home(g, cx - 9, iy, 18, col, 2f); break;
                case 1: G.video(g, cx - 10, iy - 1, 20, col, 2f); break;
                case 2: G.flag(g, cx - 5, iy, 18, col, 2f); break;
                default: G.chart(g, cx - 9, iy, 18, col, 2f);
            }
            G.textC(g, names[i], cx, y + 40, G.b(11), col);
            final int idx = i;
            c.hit(cell * i, y, cell, 84, () -> {
                if (idx == 3) App.lastSession = App.sessions.get(0);
                if (idx == 1) { App.customQuestions = null; App.bossMode = false; App.go(dest[idx]); }
                else App.tab(dest[idx]);
            });
        }
    }

    /** 공통 상단: 뒤로가기 + 제목 + 부제 */
    public static void header(Graphics2D g, Canvas c, String title, String sub, Color ink, Color subCol) {
        G.back(g, 28, sub == null ? 58 : 58, ink);
        c.hit(10, 40, 44, 50, App::back);
        if (sub == null) {
            G.text(g, title, 56, 49, G.b(19), ink);
        } else {
            G.text(g, title, 56, 44, G.b(19), ink);
            G.text(g, sub, 56, 70, G.f(11.5f), subCol);
        }
    }

    /** 하단 큰 버튼 */
    public static void bigButton(Graphics2D g, Canvas c, String label, Color bg, Runnable action) {
        G.rr(g, 18, H - 80, W - 36, 50, 13, bg);
        G.textBox(g, label, 18, H - 80, W - 36, 50, G.b(16), G.WHITE);
        c.hit(18, H - 80, W - 36, 50, action);
    }

    /** 반투명 안내 메시지 */
    static class Toast extends JComponent {
        private String msg;
        private javax.swing.Timer timer;
        Toast() { setOpaque(false); }
        void show(String m) {
            msg = m;
            setVisible(true);
            repaint();
            if (timer != null) timer.stop();
            timer = new javax.swing.Timer(1700, e -> { msg = null; setVisible(false); });
            timer.setRepeats(false);
            timer.start();
        }
        @Override public boolean contains(int x, int y) { return false; }  // 클릭은 아래 화면으로 통과
        @Override protected void paintComponent(Graphics g0) {
            if (msg == null) return;
            Graphics2D g = (Graphics2D) g0; G.smooth(g);
            Point p = SwingUtilities.convertPoint(root, 0, 0, this);
            g.translate(p.x, p.y);
            g.scale(S, S);
            int w = Math.min(W - 40, G.width(g, msg, G.b(13)) + 36);
            G.rr(g, (W - w) / 2.0, H - 150, w, 40, 20, new Color(17, 24, 39, 230));
            G.textBox(g, msg, (W - w) / 2.0, H - 150, w, 40, G.b(13), G.WHITE);
        }
    }
}
