package interviewmate;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** 12. 내 자소서 예상 질문 */
public class ExpectedScreen extends Page {
    private String filter = "전체";
    private final boolean[] open = new boolean[Data.EXPECTED.length];

    public ExpectedScreen() {
        super(G.BG, 146, App.H - 96, 1200);
        open[0] = true;
    }

    private List<Data.Expected> shown() {
        List<Data.Expected> l = new ArrayList<>();
        for (Data.Expected e : Data.EXPECTED) if (filter.equals("전체") || e.cat.equals(filter)) l.add(e);
        return l;
    }

    @Override protected void draw(Graphics2D g) {
        App.header(g, this, "내 자소서 예상 질문", "면접관은 자소서의 이 문장들을 파고들 거예요", G.INK, G.SUB);
        String[] cats = {"전체", "경험 검증", "꼬리질문", "약점 공략"};
        double x = 18.5;
        for (String c : cats) {
            int n = 0;
            for (Data.Expected e : Data.EXPECTED) if (c.equals("전체") || e.cat.equals(c)) n++;
            String label = c + " " + n;
            int w = G.width(g, label, G.b(12.5f)) + 24;
            SelectScreen.chipSmall(g, label, x, 96, w, filter.equals(c));
            hit(x, 96, w, 37, () -> { filter = c; scrollTop(); });
            x += w + 6;
        }
        App.bigButton(g, this, "전체 질문으로 모의 면접 보기", G.BLUE, () -> {
            List<String> q = new ArrayList<>();
            for (Data.Expected e : shown()) q.add(e.q);
            App.customQuestions = q;
            App.bossMode = false;
            App.go("select");
        });
    }

    @Override protected void drawBody(Graphics2D g, Body b) {
        double y = 4;
        for (Data.Expected e : shown()) {
            int idx = java.util.Arrays.asList(Data.EXPECTED).indexOf(e);
            int qh = G.split(g, e.q, 290, G.b(15)).size() * 23;
            int gh = open[idx] ? G.split(g, e.guide, 268, G.f(12.5f)).size() * 20 + 44 : 0;
            double h = 18 + 26 + 12 + 56 + 14 + qh + 12 + (open[idx] ? gh + 10 : 0) + 37 + 18;
            G.rr(g, 18.5, y, 325, h, 17, G.WHITE);
            double cy = y + 18;
            Color[] c = catColor(e.cat);
            double tx = 33 + G.tag(g, e.cat, 33, cy, 26, G.b(11.5f), c[0], c[1], 8, 7) + 6;
            if (e.priority) G.tag(g, "우선 연습", tx, cy, 26, G.b(11.5f), G.INK, G.WHITE, 8, 7);
            cy += 38;
            G.rr(g, 33, cy, 296, 56, 10, new Color(0xF7F8FA));
            G.dashed(g, 33, cy, 296, 56, 10, G.LINE);
            G.text(g, e.from, 45, cy + 9, G.b(11), G.SLATE);
            G.text(g, e.quote, 45, cy + 29, G.f(12.5f), G.TEXT);
            cy += 70;
            G.wrap(g, e.q, 33, cy, 290, G.b(15), G.INK, 23);
            cy += qh + 12;
            if (open[idx]) {
                G.rr(g, 33, cy, 296, gh, 13, G.BLUE_SOFT);
                G.text(g, "답변 가이드", 45, cy + 14, G.b(12), new Color(0x1E3A8A));
                G.wrap(g, e.guide, 45, cy + 37, 268, G.f(12.5f), G.TEXT, 20);
                cy += gh + 10;
            }
            G.rr(g, 33, cy, 80, 37, 11, G.WHITE);
            G.rrLine(g, 33, cy, 80, 37, 11, G.LINE, 1.2f);
            G.textBox(g, open[idx] ? "가이드 접기" : "답변 가이드", 33, cy, 80, 37, G.b(12.5f), G.INK);
            b.on(33, cy, 80, 37, () -> open[idx] = !open[idx]);
            G.rr(g, 121, cy, 208, 37, 11, G.BLUE_SOFT);
            String label = "이 질문 연습하기";
            int w = G.width(g, label, G.b(13));
            double lx = 225 - (w + 14) / 2.0;
            G.circle(g, lx + 3, cy + 18.5, 3.5, new Color(0x1E3A8A));
            G.text(g, label, lx + 14, cy + 9, G.b(13), new Color(0x1E3A8A));
            b.on(121, cy, 208, 37, () -> {
                App.customQuestions = new ArrayList<>(java.util.Collections.singletonList(e.q));
                App.bossMode = false;
                App.go("select");
            });
            y += h + 12;
        }
        b.height((int) y + 6);
    }

    private Color[] catColor(String cat) {
        if (cat.equals("경험 검증")) return new Color[]{G.BLUE_SOFT, new Color(0x1E3A8A)};
        if (cat.equals("꼬리질문")) return new Color[]{G.GREEN_SOFT, G.GREEN};
        return new Color[]{G.ORANGE_SOFT, G.ORANGE};
    }
}
