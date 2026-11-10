package interviewmate;

import java.awt.*;
import java.util.List;

/** 11. 자기소개서 피드백 */
public class ResumeScreen extends Page {
    private int tab = 0;
    private final List<Data.Sentence>[] sections;
    private final int[][] scores = new int[3][];
    private final int[] selFix = {-1, -1, -1};

    @SuppressWarnings("unchecked")
    public ResumeScreen() {
        super(G.BG, 90, App.H - 96, 820);
        sections = new List[3];
        for (int i = 0; i < 3; i++) { sections[i] = Data.resumeSection(i); scores[i] = Data.RESUME_SCORES[i].clone(); }
    }

    private int currentFix() {
        List<Data.Sentence> s = sections[tab];
        if (selFix[tab] >= 0 && selFix[tab] < s.size() && s.get(selFix[tab]).kind == 1) return selFix[tab];
        // 기본: 두 번째 고칠 문장(목업과 같게 '추상적인 성과')이 있으면 그것, 없으면 첫 번째
        int first = -1, count = 0;
        for (int i = 0; i < s.size(); i++) if (s.get(i).kind == 1) {
            if (first < 0) first = i;
            if (++count == 2 && tab == 0) return i;
        }
        return first;
    }

    @Override protected void draw(Graphics2D g) {
        App.header(g, this, "자기소개서 피드백", "[회사명] 백엔드 개발자 · 3문항", G.INK, G.SUB);
        G.share(g, 321, 52, G.INK);
        hit(310, 44, 40, 40, () -> App.toast("공유 링크를 복사했어요"));
        G.rr(g, 18.5, App.H - 80, 325, 50, 13, G.BLUE);
        String label = "이 자소서로 예상 질문 뽑기";
        int w = G.width(g, label, G.b(16));
        double x = App.W / 2.0 - (w + 22) / 2.0;
        G.text(g, label, x, App.H - 66, G.b(16), G.WHITE);
        G.arrowRight(g, x + w + 8, App.H - 55, 12, G.WHITE, 2f);
        hit(18.5, App.H - 80, 325, 50, () -> App.go("expected"));
    }

    @Override protected void drawBody(Graphics2D g, Body b) {
        // 탭
        for (int i = 0; i < 3; i++) {
            double tx = 18.5 + i * 110.5;
            boolean on = tab == i;
            if (on) G.rr(g, tx, 6, 104, 40, 12, G.INK);
            else { G.rr(g, tx, 6, 104, 40, 12, G.WHITE); G.rrLine(g, tx, 6, 104, 40, 12, G.LINE, 1.2f); }
            G.textBox(g, Data.RESUME_TABS[i], tx, 6, 104, 40, G.b(13), on ? G.WHITE : G.TEXT);
            final int k = i;
            b.on(tx, 6, 104, 40, () -> tab = k);
        }
        // 점수
        String[] names = {"구체성", "직무 연관", "가독성", "차별성"};
        G.rr(g, 18.5, 61, 325, 70, 15, G.WHITE);
        for (int i = 0; i < 4; i++) {
            double cx = 62 + i * 75;
            int v = scores[tab][i];
            G.textC(g, String.valueOf(v), cx, 72, G.b(17), v < 65 ? G.ORANGE : G.BLUE);
            G.textC(g, names[i], cx, 101, G.f(11.5f), G.SLATE);
        }

        // 본문 (고칠 문장 / 살릴 문장 형광펜)
        List<Data.Sentence> ss = sections[tab];
        int chars = 0;
        for (Data.Sentence s : ss) chars += s.text.length();
        double top = 144;
        double textH = flow(g, b, ss, top + 48, false);
        double cardH = 48 + textH + 50;
        G.rr(g, 18.5, top, 325, cardH, 17, G.WHITE);
        G.text(g, Data.RESUME_TITLES[tab], 33, top + 18, G.b(13.5f), G.INK);
        G.textR(g, Math.min(800, 400 + chars * 2) + " / 800자", 329, top + 19, G.f(12), G.SLATE);
        flow(g, b, ss, top + 48, true);
        double ly = top + cardH - 34;
        G.rr(g, 33, ly, 13, 13, 3, new Color(0xFED7AA));
        G.text(g, "고칠 문장", 52, ly - 2, G.f(11.5f), G.SLATE);
        G.rr(g, 118, ly, 13, 13, 3, new Color(0xC7D2FE));
        G.text(g, "살릴 문장", 137, ly - 2, G.f(11.5f), G.SLATE);
        double y = top + cardH + 14;

        // 고칠 점 카드
        int fi = currentFix();
        if (fi >= 0) {
            Data.Sentence s = ss.get(fi);
            int rh = G.split(g, s.fixReason, 296, G.f(12.5f)).size() * 20;
            int sh = G.split(g, s.suggestion, 266, G.b(14)).size() * 22;
            double innerH = 30 + sh + 54;
            double h = 38 + rh + 12 + innerH + 16;
            G.rr(g, 18.5, y, 325, h, 17, G.CREAM);
            G.text(g, "고칠 점 · " + s.fixTitle, 33, y + 15, G.b(12), G.ORANGE);
            G.wrap(g, s.fixReason, 33, y + 40, 296, G.f(12.5f), G.TEXT, 20);
            double iy = y + 40 + rh + 12;
            G.rr(g, 33, iy, 296, innerH, 13, G.WHITE);
            G.text(g, "이렇게 바꿔 보세요", 47, iy + 14, G.b(11), G.SLATE);
            G.wrap(g, s.suggestion, 47, iy + 36, 266, G.b(14), G.INK, 22);
            double by = iy + 36 + sh + 8;
            G.rr(g, 47, by, 74, 32, 10, G.WHITE);
            G.rrLine(g, 47, by, 74, 32, 10, G.LINE, 1.2f);
            G.textBox(g, "문장 바꾸기", 47, by, 74, 32, G.b(12), G.INK);
            b.on(47, by, 74, 32, () -> {
                boolean lead = s.text.startsWith(" ");
                s.text = (lead ? " " : "") + s.suggestion;
                s.kind = 2;
                selFix[tab] = -1;
                scores[tab][0] = Math.min(95, scores[tab][0] + 9);
                scores[tab][1] = Math.min(95, scores[tab][1] + 5);
                scores[tab][3] = Math.min(95, scores[tab][3] + 8);
                App.toast("문장을 바꿨어요");
            });
            y += h + 16;
        } else {
            G.rr(g, 18.5, y, 325, 64, 17, G.GREEN_SOFT);
            G.text(g, "고칠 문장을 모두 바꿨어요!", 33, y + 14, G.b(13.5f), G.GREEN);
            G.text(g, "이제 예상 질문으로 면접을 준비해 보세요.", 33, y + 36, G.f(12), G.TEXT);
            y += 80;
        }
        b.height((int) y + 6);
    }

    /** 문장 흐름 배치. paint=false 면 높이만 계산 */
    private double flow(Graphics2D g, Body b, List<Data.Sentence> ss, double top, boolean paint) {
        Font f = G.f(13.5f);
        FontMetrics fm = g.getFontMetrics(f);
        double left = 33, right = 329, lh = 29, x = left, y = top;
        for (int si = 0; si < ss.size(); si++) {
            Data.Sentence s = ss.get(si);
            String t = s.text;
            if (t.startsWith(" ")) { x += fm.charWidth(' ') + 4; t = t.substring(1); if (x >= right) { x = left; y += lh; } }
            StringBuilder run = new StringBuilder();
            double runX = x;
            for (int i = 0; i < t.length(); i++) {
                char ch = t.charAt(i);
                double cw = fm.charWidth(ch);
                if (x + cw > right) {
                    if (paint) paintRun(g, b, run.toString(), runX, y, s, si, f);
                    run.setLength(0);
                    x = left; y += lh; runX = x;
                    if (ch == ' ') continue;
                }
                run.append(ch);
                x += cw;
            }
            if (paint) paintRun(g, b, run.toString(), runX, y, s, si, f);
        }
        return y - top + lh;
    }

    private void paintRun(Graphics2D g, Body b, String run, double x, double y, Data.Sentence s, int si, Font f) {
        if (run.isEmpty()) return;
        double w = G.width(g, run, f);
        if (s.kind == 1) {
            boolean sel = si == currentFix();
            G.rr(g, x - 2, y - 3, w + 4, 25, 3, sel ? new Color(0xFDBA74) : new Color(0xFED7AA));
            G.rr(g, x - 2, y + 20, w + 4, 2, 1, G.ORANGE);
            b.on(x - 2, y - 3, w + 4, 25, () -> selFix[tab] = si);
        } else if (s.kind == 2) {
            G.rr(g, x - 2, y - 3, w + 4, 25, 3, new Color(0xC7D2FE));
            G.rr(g, x - 2, y + 20, w + 4, 2, 1, G.BLUE);
        }
        G.text(g, run, x, y, f, G.INK);
    }
}
