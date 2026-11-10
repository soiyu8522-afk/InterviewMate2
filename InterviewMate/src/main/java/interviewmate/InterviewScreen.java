package interviewmate;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

/** 4. 면접 진행 + 8. 돌발 상황 카드 */
public class InterviewScreen extends Canvas {
    private List<String> qs;
    private int type, idx, seconds, answered, habit;
    private boolean panel, muted, replaying;
    private int replayLeft;
    private String gazeMsg = "카메라를 봐 주세요", speedMsg = "적당해요";
    private boolean gazeOk = false;
    private int[] surprise = null;      // {카드 번호, 남은 초}
    private int surpriseNo = 0;
    private final Random rnd = new Random();
    private final Timer timer = new Timer(1000, e -> tick());
    // 마이크로 잰 값: 전체 샘플 수, 말한 샘플 수, 최근 말한 비율, 침묵 시간(초)
    private int samples, spokenSamples;
    private double silence;

    public InterviewScreen() {
        super(App.W, App.H);
        setBackground(G.DARK);
        // 0.06초마다: 카메라 화면 갱신 + 마이크 측정
        new Timer(60, e -> {
            if (!isShowing() || !timer.isRunning()) return;
            if (Media.micOk && !muted && surprise == null) {
                samples++;
                if (Media.speaking()) { spokenSamples++; silence = 0; }
                else silence += 0.06;
            }
            repaint();
        }).start();
    }

    private boolean micLive() { return Media.micOk && !muted; }

    /** 면접 시작 준비 */
    public void start() {
        if (App.bossMode) {
            type = 4;
            qs = Data.questionsFor(4, 10);
        } else {
            type = App.interviewer;
            qs = App.customQuestions != null ? App.customQuestions : Data.questionsFor(type, App.questionCount);
        }
        idx = 0; seconds = 0; answered = 0; habit = 0; surpriseNo = 0;
        samples = 0; spokenSamples = 0; silence = 0;
        panel = App.bossMode; muted = false; surprise = null;
        timer.restart();
        if (App.tts) speak();
    }

    private void speak() { replaying = true; replayLeft = 2; }

    private void tick() {
        seconds++;
        if (replaying && --replayLeft <= 0) replaying = false;
        // 실시간 피드백 흉내 (실제 앱에서는 카메라·마이크 분석 결과가 들어갈 자리)
        if (App.realtime && seconds % 3 == 0) {
            gazeOk = rnd.nextInt(3) > 0;
            gazeMsg = gazeOk ? "좋아요, 유지하세요" : "카메라를 봐 주세요";
            String[] sp = {"적당해요", "적당해요", "조금 빨라요", "조금 느려요"};
            speedMsg = sp[rnd.nextInt(sp.length)];
            // 습관어는 음성인식이 필요해서 흉내만 냄 (마이크가 있으면 말하는 동안에만 늘어남)
            if (!muted && (!Media.micOk || Media.speaking()) && rnd.nextInt(4) == 0) habit++;
        }
        if (surprise != null && --surprise[1] <= 0) acceptSurprise();
        repaint();
    }

    private void next() {
        if (surprise != null) return;
        answered++;
        if (type == 1) App.progress("press", 1);
        if (idx == 0 && qs.get(0).contains("자기소개")) App.progress("intro", 1);
        idx++;
        if (idx >= qs.size()) { finish(); return; }
        habit = 0;
        silence = 0;
        if (App.tts) speak();
        boolean show = App.bossMode ? (idx == 2 || idx == 6)
                : App.surpriseFreq == 1 ? idx == 2
                : App.surpriseFreq == 2 && idx % 2 == 0;
        if (show) surprise = new int[]{surpriseNo == 0 ? 0 : rnd.nextInt(Data.SURPRISES.length), 30};
    }

    private void acceptSurprise() {
        if (surprise[0] == 0) panel = true;
        surprise = null;
        surpriseNo++;
    }

    private void finish() {
        timer.stop();
        Data.Session s = Data.makeSession(type, qs, App.realtime, answered);
        if (samples > 50) {
            // 실제로 마이크로 잰 '말한 시간 비율'로 말 속도 점수를 정함 (40~70%가 가장 좋음)
            double ratio = spokenSamples / (double) samples;
            int score = (int) Math.round(95 - Math.abs(ratio - 0.55) * 90);
            s.scores[2] = Math.max(40, Math.min(98, score));
        }
        s.snap = Media.frame;
        App.sessions.add(0, s);
        App.lastSession = s;
        App.progress("mock", 1);
        if (s.scores[0] >= 80) App.progress("gaze", 1);
        App.weekDone[LocalDate.now().getDayOfWeek().getValue() - 1] = true;
        if (App.bossMode) {
            int gauge = Math.min(100, s.total() + answered);
            App.bossBest = Math.max(App.bossBest, gauge);
            if (gauge >= 80 && !App.bossCleared) {
                App.bossCleared = true;
                App.executiveUnlocked = true;
                App.addXp(500);
                App.toast("보스전 클리어! 깐깐한 임원형이 열렸어요");
            } else App.toast("설득 게이지 " + gauge + "% · 합격선 80%");
            App.bossMode = false;
        }
        App.customQuestions = null;
        App.tab("report");
    }

    private void stop() {
        int r = JOptionPane.showConfirmDialog(this, "면접을 종료할까요?\n답변한 문항까지 리포트를 만들어요.",
                "면접 종료", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) return;
        if (answered == 0) { timer.stop(); App.customQuestions = null; App.bossMode = false; App.tab("home"); }
        else finish();
    }

    @Override protected void draw(Graphics2D g) {
        Data.Type t = Data.TYPES[type];
        // 상단 상태 줄
        int nw = G.width(g, App.bossMode ? "깐깐한 임원 3인" : t.name, G.b(12.5f)) + 40;
        G.rr(g, 15, 48, nw, 30, 15, new Color(0x1E2433));
        if (surprise == null) G.circle(g, 30, 63, 4.5, new Color(0xF87171));
        G.text(g, App.bossMode ? "깐깐한 임원 3인" : t.name, surprise == null ? 41 : 27, 55, G.b(12.5f), new Color(0xE5E7EB));
        G.textC(g, "질문 " + (idx + 1) + " / " + qs.size(), 186, 54, G.b(13), new Color(0xD1D5DB));
        G.rr(g, 254, 48, 93, 30, 15, new Color(0x4A1515));
        String rec = String.format("REC %02d:%02d", seconds / 60, seconds % 60);
        if (seconds % 2 == 0) G.circle(g, 267, 63, 4.5, new Color(0xEF4444));
        G.text(g, rec, 277, 55, G.b(12.5f), new Color(0xFCA5A5));

        if (panel || (surprise != null && surprise[0] == 0)) drawPanel(g); else drawRoom(g, t);

        // 질문 카드
        G.rr(g, 15, 457, 332, 103, 15, G.DARK_CARD);
        G.wrap(g, qs.get(idx), 30, 472, 300, G.b(15), G.WHITE, 21);
        String hint = muted ? "마이크가 꺼져 있어요" : replaying ? "질문을 읽는 중…"
                : Media.speaking() ? "말하는 중…" : "답변을 시작하세요";
        G.text(g, hint, 30, 520, G.f(11.5f), new Color(0x9CA3AF));
        G.rrLine(g, 214, 508, 118, 37, 18.5, new Color(0x4B5563), 1.2f);
        G.speaker(g, 230, 526.5, G.WHITE);
        G.text(g, "질문 다시 듣기", 250, 517, G.b(12.5f), G.WHITE);
        hit(214, 508, 118, 37, this::speak);

        // 실시간 피드백 칩
        if (App.realtime) {
            chip(g, 15, "시선", gazeMsg, gazeOk ? new Color(0x14261A) : new Color(0x2E2016),
                    gazeOk ? G.GREEN_BRIGHT : G.ORANGE_BRIGHT);
            String voice = speedMsg;
            if (micLive()) {    // 마이크로 실제 측정
                if (silence >= 3) voice = "침묵 " + (int) silence + "초";
                else if (Media.speaking() && Media.level < 0.12f) voice = "조금 작아요";
                else voice = "적당해요";
            }
            boolean spOk = voice.equals("적당해요");
            chip(g, 128, micLive() ? "목소리" : "말 속도", voice, spOk ? new Color(0x14261A) : new Color(0x2E2016),
                    spOk ? G.GREEN_BRIGHT : G.ORANGE_BRIGHT);
            chip(g, 241, "습관어", "“음…” " + habit + "회", habit < 3 ? new Color(0x14261A) : new Color(0x2E2016),
                    habit < 3 ? G.GREEN_BRIGHT : G.ORANGE_BRIGHT);
        } else {
            G.rr(g, 15, 572, 332, 75, 13, new Color(0x161B26));
            G.monitor(g, 33, 600, new Color(0x93B4FA));
            G.text(g, "녹화 후 피드백 모드", 62, 590, G.b(13), G.WHITE);
            G.text(g, "면접에만 집중하세요. 끝나면 리포트로 알려 드려요.", 62, 610, G.f(11.5f), new Color(0x9CA3AF));
        }

        // 하단 버튼
        G.circle(g, 40, 729, 25, new Color(0x1E2433));
        if (micLive() && Media.level > 0.02f)   // 소리 크기만큼 초록 테두리
            G.ring(g, 40, 729, 25 + Media.level * 6, new Color(74, 222, 128, 90 + (int) (Media.level * 160)), 2.5f);
        G.mic(g, 40, 729, G.WHITE, muted);
        hit(15, 704, 50, 50, () -> { muted = !muted; App.toast(muted ? "마이크를 껐어요" : "마이크를 켰어요"); });
        boolean last = idx == qs.size() - 1;
        G.rr(g, 74, 704, 211, 50, 13, G.BLUE);
        G.textBox(g, last ? "답변 완료 · 리포트 보기" : "답변 완료 · 다음 질문", 74, 704, 211, 50, G.b(15), G.WHITE);
        hit(74, 704, 211, 50, this::next);
        G.rr(g, 294, 704, 53, 50, 13, new Color(0x2A1414));
        G.textBox(g, "종료", 294, 704, 53, 50, G.b(14), new Color(0xFCA5A5));
        hit(294, 704, 53, 50, this::stop);

        if (surprise != null) drawSurprise(g);
    }

    private void chip(Graphics2D g, double x, String label, String value, Color bg, Color fg) {
        G.rr(g, x, 572, 106, 75, 13, bg);
        G.text(g, label, x + 10, 583, G.f(11), new Color(0x9CA3AF));
        G.wrap(g, value, x + 10, 602, 88, G.b(13), fg, 18);
    }

    /** 면접실 배경 + 면접관 1명 */
    private void drawRoom(Graphics2D g, Data.Type t) {
        Shape old = g.getClip();
        g.clip(new Rectangle2D.Double(0, 89, App.W, 353));
        G.rr(g, 0, 89, App.W, 353, 0, new Color(0x2A3249));
        // 창문
        G.rr(g, 219, 123, 113, 139, 4, new Color(0x3A4566));
        G.rr(g, 225, 129, 46, 60, 0, new Color(0x45527A));
        G.rr(g, 278, 129, 48, 60, 0, new Color(0x45527A));
        G.rr(g, 225, 195, 46, 62, 0, new Color(0x45527A));
        G.rr(g, 278, 195, 48, 62, 0, new Color(0x45527A));
        // 선반, 책, 화분
        G.rr(g, 26, 197, 104, 5, 2, new Color(0x5A4636));
        G.rr(g, 35, 161, 11, 36, 1, new Color(0xA0613B));
        G.rr(g, 48, 169, 9, 28, 1, new Color(0x6A7AA8));
        G.rr(g, 59, 158, 13, 39, 1, new Color(0xB9A47C));
        G.circle(g, 109, 180, 13, new Color(0x4C8A55));
        G.rr(g, 102, 186, 15, 11, 1, new Color(0x8A5A3C));
        // 면접관
        Faces.Expr e = t.expr;
        Faces.person(g, 181, 272, 43, new Color(0x1F2937), t.hair, e, t.glasses, true, 400);
        // 책상과 명패
        G.rr(g, 0, 388, App.W, 54, 0, new Color(0x473A30));
        G.rr(g, 0, 388, App.W, 5, 0, new Color(0x5A4A3C));
        G.rr(g, 139, 403, 84, 24, 3, new Color(0xE5E7EB));
        G.textBox(g, "면접관", 139, 403, 84, 24, G.b(11.5f), G.INK);
        g.setClip(old);
        drawSelf(g);
    }

    /** 내 카메라 (작은 화면) */
    private void drawSelf(Graphics2D g) {
        RoundRectangle2D pip = new RoundRectangle2D.Double(263, 101, 88, 117, 22, 22);
        g.setColor(new Color(0x2A3247));
        g.fill(pip);
        Shape old = g.getClip();
        g.clip(pip);
        if (!Media.draw(g, pip, 263, 101, 88, 117)) Faces.silhouette(g, 307, 162, 16, new Color(0x6B7280));
        g.setClip(old);
        G.rrLine(g, 281, 116, 48, 44, 7, gazeOk || !App.realtime ? new Color(0x4ADE80) : G.ORANGE_BRIGHT, 1.6f);
        G.rr(g, 270, 195, 19, 17, 4, new Color(0x111827));
        G.textBox(g, "나", 270, 195, 19, 17, G.b(10), G.WHITE);
        g.setColor(G.WHITE);
        g.setStroke(new BasicStroke(1.6f));
        g.draw(pip);
    }

    /** 3:1 면접 화면 */
    private void drawPanel(Graphics2D g) {
        RoundRectangle2D box = new RoundRectangle2D.Double(15, 89, 332, 278, 30, 30);
        g.setPaint(new GradientPaint(0, 89, new Color(0x1C2233), 0, 367, new Color(0x232A3B)));
        g.fill(box);
        Shape old = g.getClip();
        g.clip(box);
        Faces.person(g, 73, 296, 24, new Color(0x161B26), Faces.HAIR, Faces.Expr.FLAT, false, false, 380);
        Faces.person(g, 181, 296, 24, new Color(0x334155), Faces.GRAY_HAIR, App.bossMode ? Faces.Expr.FROWN : Faces.Expr.FLAT, false, false, 380);
        Faces.person(g, 289, 296, 24, new Color(0x4A3A2C), new Color(0x3B2A1C), Faces.Expr.FLAT, false, false, 380);
        g.setClip(old);
        if (surprise == null) drawSelfSmall(g);
    }

    private void drawSelfSmall(Graphics2D g) {
        RoundRectangle2D pip = new RoundRectangle2D.Double(283, 99, 56, 72, 16, 16);
        g.setColor(new Color(0x2A3247));
        g.fill(pip);
        Shape old = g.getClip();
        g.clip(pip);
        if (!Media.draw(g, pip, 283, 99, 56, 72)) Faces.silhouette(g, 311, 136, 10, new Color(0x6B7280));
        g.setClip(old);
        g.setColor(G.WHITE);
        g.setStroke(new BasicStroke(1.4f));
        g.draw(pip);
    }

    private void drawSurprise(Graphics2D g) {
        String[] c = Data.SURPRISES[surprise[0]];
        // 뒤 화면 어둡게
        G.rr(g, 0, 380, App.W, 330, 0, new Color(13, 17, 23, 215));
        G.rr(g, 0, 89, App.W, 300, 0, new Color(13, 17, 23, 70));
        // 카드
        G.rr(g, 15, 417, 332, 288, 22, new Color(0xFFF5EC));
        G.bolt(g, 34, 449, 13, G.ORANGE);
        G.text(g, "돌발 상황  " + (surpriseNo + 1) + "  /  5", 54, 449, G.b(11.5f), G.ORANGE);
        // 남은 시간 원
        G.ring(g, 308, 456, 18.5, new Color(0xF4D3C0), 3.5f);
        g.setColor(G.ORANGE);
        g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Arc2D.Double(289.5, 437.5, 37, 37, 90, 360 * surprise[1] / 30.0, Arc2D.OPEN));
        G.textBox(g, String.valueOf(surprise[1]), 290, 438, 37, 37, G.b(12), G.ORANGE);

        G.text(g, c[0], 34, 487, G.b(18.5f), G.INK);
        int h = G.wrap(g, c[1], 34, 527, 296, G.f(12.5f), G.TEXT, 20);
        double ty = Math.max(577, 527 + h + 10);
        G.rr(g, 34, ty, 295, 54, 11, G.WHITE);
        G.text(g, "대응 팁", 45, ty + 9, G.b(12), G.BLUE);
        G.wrap(g, c[2], 85, ty + 9, 235, G.f(12), G.TEXT, 18);
        G.rr(g, 34, 643, 80, 44, 13, G.WHITE);
        G.rrLine(g, 34, 643, 80, 44, 13, G.LINE, 1.2f);
        G.textBox(g, "다른 카드", 34, 643, 80, 44, G.b(14), G.INK);
        hit(34, 643, 80, 44, () -> surprise[0] = (surprise[0] + 1 + rnd.nextInt(Data.SURPRISES.length - 1)) % Data.SURPRISES.length);
        G.rr(g, 122, 643, 207, 44, 13, G.ORANGE);
        G.textBox(g, "받아들이고 답변하기", 122, 643, 207, 44, G.b(15), G.WHITE);
        hit(122, 643, 207, 44, this::acceptSurprise);

        // 돌발 빈도
        G.rr(g, 0, 710, App.W, 74, 0, G.DARK);
        G.text(g, "돌발 빈도", 15, 729, G.b(11.5f), new Color(0xD1D5DB));
        G.rr(g, 67, 717, 280, 40, 11, new Color(0x1F2433));
        String[] f = {"없음", "가끔", "자주"};
        for (int i = 0; i < 3; i++) {
            double x = 71 + i * 91.3;
            if (App.surpriseFreq == i) {
                G.rr(g, x, 721, 88, 32, 8, G.WHITE);
                G.textBox(g, f[i], x, 721, 88, 32, G.b(13), G.INK);
            } else G.textBox(g, f[i], x, 721, 88, 32, G.b(13), new Color(0xD1D5DB));
            final int k = i;
            hit(x, 717, 88, 40, () -> App.surpriseFreq = k);
        }
    }
}
