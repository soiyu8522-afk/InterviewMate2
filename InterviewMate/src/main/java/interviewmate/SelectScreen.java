package interviewmate;

import java.awt.*;

/** 2. 면접관 선택 (1/2 단계) */
public class SelectScreen extends Page {
    public SelectScreen() { super(G.BG, 0, App.H - 96, 930); }

    @Override public void onShow() { scrollTop(); }

    @Override protected void draw(Graphics2D g) {
        String label = App.customQuestions != null ? "다음: 피드백 방식 고르기" : "다음: 피드백 방식 고르기";
        G.rr(g, 18.5, App.H - 80, 325, 50, 13, G.BLUE);
        int w = G.width(g, label, G.b(16));
        double x = App.W / 2.0 - (w + 22) / 2.0;
        G.text(g, label, x, App.H - 66, G.b(16), G.WHITE);
        G.arrowRight(g, x + w + 8, App.H - 55, 12, G.WHITE, 2f);
        hit(18.5, App.H - 80, 325, 50, () -> App.go("feedback"));
    }

    @Override protected void drawBody(Graphics2D g, Body b) {
        G.back(g, 28, 58, G.INK);
        b.on(10, 40, 44, 50, App::back);
        G.text(g, "1 / 2 단계", 59, 56, G.b(12.5f), G.SLATE);
        G.text(g, "어떤 면접관과 연습할까요?", 18, 94, G.b(21.5f), G.INK);
        String sub = App.customQuestions != null
                ? "맞춤 질문 " + App.customQuestions.size() + "개로 면접을 봐요"
                : "성향에 따라 질문 방식과 반응이 달라져요";
        G.text(g, sub, 18, 128, G.f(12.5f), G.SLATE);

        // 성향 카드 2 x 2
        for (int i = 0; i < 4; i++) {
            double x = 18.5 + (i % 2) * 167.5, y = 163 + (i / 2) * 151;
            card(g, b, i, x, y, 158, 141);
        }
        double y = 472;
        // 잠금 해제형 면접관
        Data.Type ex = Data.TYPES[4];
        boolean sel = App.interviewer == 4;
        if (sel) G.rr(g, 15.5, y - 3, 331, 70, 18, new Color(0xDCE4FB));
        G.rr(g, 18.5, y, 325, 64, 15, G.WHITE);
        if (sel) G.rrLine(g, 18.5, y, 325, 64, 15, G.BLUE, 2f);
        Faces.avatar(g, 50, y + 32, 19, ex);
        if (App.executiveUnlocked) {
            G.text(g, ex.name, 80, y + 13, G.b(14), G.INK);
            G.text(g, ex.desc, 80, y + 35, G.f(11.5f), G.SUB);
            G.radio(g, 318, y + 32, 9, sel);
            b.on(18.5, y, 325, 64, () -> App.interviewer = 4);
        } else {
            g.setComposite(AlphaComposite.SrcOver.derive(0.55f));
            G.text(g, ex.name + "  · 잠김", 80, y + 13, G.b(14), G.INK);
            g.setComposite(AlphaComposite.SrcOver);
            G.text(g, "주말 보스전을 클리어하면 열려요", 80, y + 35, G.f(11.5f), G.SUB);
            G.tag(g, "보스전", 280, y + 20, 24, G.b(11), G.ORANGE_SOFT, G.ORANGE, 9, 12);
            b.on(18.5, y, 325, 64, () -> App.go("boss"));
        }
        y += 82;

        // 질문 예시
        Data.Type t = Data.TYPES[App.interviewer];
        G.rr(g, 18.5, y, 325, 124, 15, G.WHITE);
        G.text(g, "이 면접관의 질문 예시", 33, y + 16, G.b(11.5f), G.SLATE);
        int qh = G.wrap(g, t.sample, 33, y + 40, 295, G.b(14.5f), G.INK, 23);
        double tx = 33;
        for (String tag : t.tags) tx += G.pill(g, tag, tx, y + 48 + qh, 24, G.b(11), t.tagBg, t.tagFg, 10) + 7;
        y += 146;

        // 지원 직무
        G.text(g, "지원 직무", 18, y, G.b(14), G.INK);
        y += 28;
        double cx = 18.5, cy = y;
        for (int i = 0; i < Data.JOBS.length; i++) {
            int w = G.width(g, Data.JOBS[i], G.b(14)) + 30;
            if (cx + w > 345) { cx = 18.5; cy += 46; }
            chip(g, Data.JOBS[i], cx, cy, w, App.job == i);
            final int k = i;
            b.on(cx, cy, w, 37, () -> App.job = k);
            cx += w + 8;
        }
        y = cy + 62;

        // 질문 개수
        G.text(g, "질문 개수", 18, y, G.b(14), G.INK);
        y += 28;
        if (App.customQuestions != null) {
            G.text(g, "맞춤 질문 " + App.customQuestions.size() + "개가 그대로 쓰여요", 18, y + 8, G.f(13), G.SUB);
        } else {
            cx = 18.5;
            for (int i = 0; i < Data.COUNTS.length; i++) {
                String s = Data.COUNTS[i] + "문항";
                int w = G.width(g, s, G.b(14)) + 30;
                chip(g, s, cx, y, w, App.questionCount == Data.COUNTS[i]);
                final int n = Data.COUNTS[i];
                b.on(cx, y, w, 37, () -> App.questionCount = n);
                cx += w + 8;
            }
        }
        b.height((int) y + 70);
    }

    private void card(Graphics2D g, Body b, int i, double x, double y, double w, double h) {
        Data.Type t = Data.TYPES[i];
        boolean sel = App.interviewer == i;
        if (sel) G.rr(g, x - 3, y - 3, w + 6, h + 6, 18, new Color(0xDCE4FB));
        G.rr(g, x, y, w, h, 15, G.WHITE);
        if (sel) G.rrLine(g, x, y, w, h, 15, G.BLUE, 2f);
        Faces.avatar(g, x + 39, y + 38, 24, t);
        G.radio(g, x + 134, y + 23, 9.5, sel);
        G.text(g, t.name, x + 15, y + 70, G.b(15), G.INK);
        G.wrap(g, t.desc, x + 15, y + 96, w - 28, G.f(12), G.SUB, 16.5);
        b.on(x, y, w, h, () -> App.interviewer = i);
    }

    static void chipSmall(Graphics2D g, String s, double x, double y, double w, boolean on) {
        if (on) {
            G.rr(g, x, y, w, 37, 18.5, G.INK);
            G.textBox(g, s, x, y, w, 37, G.b(12.5f), G.WHITE);
        } else {
            G.rr(g, x, y, w, 37, 18.5, G.WHITE);
            G.rrLine(g, x, y, w, 37, 18.5, G.LINE, 1.2f);
            G.textBox(g, s, x, y, w, 37, G.b(12.5f), G.TEXT);
        }
    }

    static void chip(Graphics2D g, String s, double x, double y, double w, boolean on) {
        if (on) {
            G.rr(g, x, y, w, 37, 18.5, G.INK);
            G.textBox(g, s, x, y, w, 37, G.b(14), G.WHITE);
        } else {
            G.rr(g, x, y, w, 37, 18.5, G.WHITE);
            G.rrLine(g, x, y, w, 37, 18.5, G.LINE, 1.2f);
            G.textBox(g, s, x, y, w, 37, G.b(14), G.TEXT);
        }
    }
}
