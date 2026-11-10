package interviewmate;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

/** 10. 채용공고 맞춤 면접 */
public class PostingScreen extends Page {
    private final JTextArea area = new JTextArea(App.postingText);
    private final Set<String> unchecked = new HashSet<>();
    private List<String[]> found = new ArrayList<>();

    private static final String SAMPLE_LINK = "[회사명] 서버 개발자 (신입)\n· Java/Spring 기반 API 개발\n· 데이터베이스 설계와 쿼리 튜닝\n· AWS 클라우드 운영, 원활한 협업";

    public PostingScreen() {
        super(G.BG, 90, App.H - 96, 900);
        area.setFont(G.f((float) (13 * App.S)));
        area.setForeground(G.TEXT);
        area.setLineWrap(true);
        area.setWrapStyleWord(false);
        area.setBorder(null);
        area.setBackground(G.WHITE);
        area.setCaretColor(G.BLUE);
        JScrollPane asp = new JScrollPane(area);
        asp.setBorder(null);
        place(asp, 33, 43, 295, 82);
        body.add(asp);
        area.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { changed(); }
            public void removeUpdate(DocumentEvent e) { changed(); }
            public void changedUpdate(DocumentEvent e) { changed(); }
        });
    }

    private void changed() { App.postingText = area.getText(); repaintAll(); }

    /** 공고에서 키워드 찾기 (실제 서비스에선 AI가 할 일을 간단한 단어 매칭으로 흉내) */
    private List<String[]> analyze() {
        List<String[]> out = new ArrayList<>();
        for (String[] k : Data.KEYWORDS)
            if (Pattern.compile(k[1], Pattern.CASE_INSENSITIVE).matcher(App.postingText).find()) out.add(k);
        return out;
    }
    private boolean isGap(String[] k) {
        return !Pattern.compile(k[1], Pattern.CASE_INSENSITIVE).matcher(App.resumeText).find();
    }
    private List<String> selected() {
        List<String> q = new ArrayList<>();
        for (String[] k : found) if (!unchecked.contains(k[0])) q.add(k[2]);
        return q;
    }

    @Override protected void draw(Graphics2D g) {
        App.header(g, this, "채용공고 맞춤 면접", null, G.INK, G.SUB);
        List<String> q = selected();
        boolean ok = !q.isEmpty();
        App.bigButton(g, this, ok ? "이 질문으로 면접관 고르기" : "질문을 1개 이상 골라 주세요", ok ? G.BLUE : new Color(0x9CA3AF), () -> {
            if (!ok) return;
            App.customQuestions = q;
            App.bossMode = false;
            App.go("select");
        });
    }

    @Override protected void drawBody(Graphics2D g, Body b) {
        found = analyze();
        G.text(g, "지원할 공고를 붙여넣어 주세요", 18, 6, G.b(14), G.INK);
        G.rr(g, 18.5, 33, 325, 100, 17, G.WHITE);
        G.rrLine(g, 18.5, 33, 325, 100, 17, G.LINE, 1.2f);

        // 링크 / 자소서 버튼
        G.rr(g, 18.5, 142, 122, 37, 13, G.WHITE);
        G.rrLine(g, 18.5, 142, 122, 37, 13, G.LINE, 1.2f);
        G.link(g, 30, 152, G.TEXT);
        G.text(g, "링크로 불러오기", 52, 151, G.b(12.5f), G.TEXT);
        b.on(18.5, 142, 122, 37, () -> {
            String url = JOptionPane.showInputDialog(this, "채용공고 링크를 붙여넣어 주세요", "https://");
            if (url != null && !url.trim().isEmpty()) {
                area.setText(SAMPLE_LINK);
                App.toast("예시 공고를 불러왔어요");
            }
        });
        G.rr(g, 148.5, 142, 113, 37, 13, G.WHITE);
        G.rrLine(g, 148.5, 142, 113, 37, 13, G.LINE, 1.2f);
        G.doc(g, 162, 152, G.TEXT);
        G.text(g, "내 자소서 연결", 182, 151, G.b(12.5f), G.TEXT);
        b.on(148.5, 142, 113, 37, () -> App.go("resume"));

        // 핵심 역량
        double y = 191;
        List<String[]> gaps = new ArrayList<>();
        for (String[] k : found) if (isGap(k)) gaps.add(k);
        double chipsH = layoutChips(g, null, 0);
        double cardH = 52 + chipsH + (gaps.isEmpty() ? 4 : 14 + gapH(g, gaps)) + 14;
        G.rr(g, 18.5, y, 325, cardH, 17, G.WHITE);
        G.text(g, "공고에서 찾은 핵심 역량", 33, y + 17, G.b(14), G.INK);
        if (found.isEmpty()) G.text(g, "공고에서 아직 역량을 찾지 못했어요", 33, y + 50, G.f(12.5f), G.SUB);
        double cy = y + 48;
        layoutChips(g, g, cy);
        cy += chipsH + 14;
        if (!gaps.isEmpty()) {
            double gh = gapH(g, gaps);
            G.rr(g, 33, cy, 296, gh, 11, G.CREAM);
            G.text(g, "빈틈", 45, cy + 11, G.b(12), G.ORANGE);
            G.wrap(g, gapText(gaps), 78, cy + 11, 240, G.f(12.5f), G.TEXT, 19);
        }
        y += cardH + 16;

        // 예상 질문
        double qTop = y;
        double listH = 0;
        for (String[] k : found) listH += G.split(g, k[2], 245, G.b(14)).size() * 21 + 38;
        G.rr(g, 18.5, qTop, 325, Math.max(80, listH + 46), 17, G.WHITE);
        G.text(g, "예상 질문", 33, y + 17, G.b(14), G.INK);
        G.textR(g, selected().size() + "개 선택됨", 329, y + 18, G.f(12), G.SLATE);
        y += 52;
        for (int i = 0; i < found.size(); i++) {
            String[] k = found.get(i);
            boolean on = !unchecked.contains(k[0]);
            G.checkbox(g, 33, y + 1, 19, on);
            int h = G.wrap(g, k[2], 63, y - 1, 245, G.b(14), G.INK, 21);
            G.text(g, "근거: " + k[3], 63, y + h + 3, G.f(11.5f), G.SUB);
            b.on(18.5, y - 8, 325, h + 34, () -> { if (on) unchecked.add(k[0]); else unchecked.remove(k[0]); });
            y += h + 38;
            if (i < found.size() - 1) G.line(g, 33, y - 14, 329, y - 14, G.TRACK, 1);
        }
        b.height((int) Math.max(qTop + 100, y + 10));
    }

    /** 역량 칩 배치. draw 가 null 이면 높이만 계산 */
    private double layoutChips(Graphics2D measure, Graphics2D draw, double top) {
        double x = 33, y = top;
        for (String[] k : found) {
            int w = G.width(measure, k[0], G.b(12)) + 26;
            if (x + w > 330) { x = 33; y += 33; }
            if (draw != null) {
                boolean gap = isGap(k);
                G.rr(draw, x, y, w, 26, 13, gap ? G.ORANGE_SOFT : G.BLUE_SOFT);
                G.textBox(draw, k[0], x, y, w, 26, G.b(12), gap ? G.ORANGE : new Color(0x1E3A8A));
            }
            x += w + 7;
        }
        return found.isEmpty() ? 26 : y - top + 26;
    }
    private String gapText(List<String[]> gaps) {
        StringBuilder sb = new StringBuilder("자소서에 ");
        for (int i = 0; i < gaps.size(); i++) sb.append(i > 0 ? ", " : "").append("‘").append(gaps.get(i)[0]).append("’");
        return sb + " 경험이 없어요. 관련 질문이 나오면 비슷한 경험으로 연결하는 연습을 넣었어요.";
    }
    private double gapH(Graphics2D g, List<String[]> gaps) {
        return G.split(g, gapText(gaps), 240, G.f(12.5f)).size() * 19 + 22;
    }
}
