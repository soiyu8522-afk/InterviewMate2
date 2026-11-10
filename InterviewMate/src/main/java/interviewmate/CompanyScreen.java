package interviewmate;

import java.awt.*;
import java.util.List;

/** 13. 기업 면접 정보 */
public class CompanyScreen extends Page {
    private int tab = 0;
    private boolean unlocked = false;
    private static final Color BROWN = new Color(0x92400E);

    public CompanyScreen() { super(G.BG, 90, App.H - 96, 720); }

    @Override protected void draw(Graphics2D g) {
        App.header(g, this, "기업 면접 정보", null, G.INK, G.SUB);
        String p = App.points + " P";
        int w = G.width(g, p, G.b(13)) + 46;
        G.rr(g, 344 - w, 51, w, 30, 15, G.YELLOW_SOFT);
        G.coin(g, 344 - w + 17, 66, BROWN);
        G.text(g, p, 344 - w + 30, 57, G.b(13), BROWN);

        G.rr(g, 18.5, App.H - 80, 98, 50, 13, G.YELLOW_SOFT);
        G.textBox(g, "후기 쓰고 +P", 18.5, App.H - 80, 98, 50, G.b(13.5f), BROWN);
        hit(18.5, App.H - 80, 98, 50, () -> App.go("review"));
        G.rr(g, 124.5, App.H - 80, 219, 50, 13, G.BLUE);
        G.textBox(g, "이 회사 면접관으로 연습", 124.5, App.H - 80, 219, 50, G.b(15), G.WHITE);
        hit(124.5, App.H - 80, 219, 50, () -> {
            App.customQuestions = new java.util.ArrayList<>(java.util.Arrays.asList(Data.COMPANY_QUESTIONS));
            App.bossMode = false;
            App.go("select");
        });
    }

    @Override protected void drawBody(Graphics2D g, Body b) {
        G.rr(g, 18.5, 6, 325, 98, 17, G.WHITE);
        G.rr(g, 33, 29, 52, 52, 13, G.NAVY);
        G.textBox(g, "LOGO", 33, 29, 52, 52, G.b(11.5f), G.WHITE);
        G.text(g, "[회사명]", 98, 22, G.b(18), G.INK);
        G.text(g, "백엔드 개발자 · 인증된 면접 후기 12개", 98, 50, G.f(12), G.SLATE);
        double tx = 98 + G.tag(g, "분위기 편안함", 98, 70, 20, G.b(11), G.GREEN_SOFT, G.GREEN, 8, 6) + 6;
        G.tag(g, "꼬리질문 많음", tx, 70, 20, G.b(11), G.ORANGE_SOFT, G.ORANGE, 8, 6);

        // 탭
        double y = 117;
        G.rr(g, 18.5, y, 325, 44, 12, new Color(0xE5E7EB));
        String[] tabs = {"면접 과정", "실제 질문", "분위기"};
        for (int i = 0; i < 3; i++) {
            double x = 22.5 + i * 106;
            if (tab == i) G.rr(g, x, y + 4, 103, 36, 9, G.WHITE);
            G.textBox(g, tabs[i], x, y + 4, 103, 36, G.b(13.5f), tab == i ? G.INK : G.SLATE);
            final int k = i;
            b.on(x, y, 103, 44, () -> tab = k);
        }
        y += 57;

        if (tab == 0) {
            G.rr(g, 18.5, y, 325, 233, 17, G.WHITE);
            G.rr(g, 45, y + 30, 4, 152, 2, new Color(0xDCE4FB));
            for (int i = 0; i < 4; i++) {
                double cy = y + 30 + i * 51;
                G.circle(g, 47, cy, 13, G.BLUE);
                G.textBox(g, String.valueOf(i + 1), 34, cy - 13, 26, 26, G.b(12), G.WHITE);
                G.text(g, Data.COMPANY_STEPS[i][0], 70, cy - 18, G.b(14.5f), G.INK);
                G.text(g, Data.COMPANY_STEPS[i][1], 70, cy + 4, G.f(12), G.SLATE);
            }
            y += 247;
        } else if (tab == 1) {
            double h = Data.COMPANY_QUESTIONS.length * 50 + 18;
            G.rr(g, 18.5, y, 325, h, 17, G.WHITE);
            for (int i = 0; i < Data.COMPANY_QUESTIONS.length; i++) {
                double qy = y + 10 + i * 50;
                G.rr(g, 30, qy, 302, 42, 10, new Color(0xF7F8FA));
                G.text(g, "Q" + (i + 1), 42, qy + 12, G.b(12), G.SLATE);
                G.text(g, Data.COMPANY_QUESTIONS[i], 68, qy + 12, G.f(12.5f), G.INK);
            }
            y += h + 14;
        } else {
            String[] k = {"편안한 분위기", "꼬리질문 빈도", "압박 강도", "기술 깊이"};
            int[] v = {72, 85, 30, 78};
            G.rr(g, 18.5, y, 325, 4 * 44 + 24, 17, G.WHITE);
            for (int i = 0; i < 4; i++) {
                double ry = y + 22 + i * 44;
                G.text(g, k[i], 33, ry, G.f(13), G.TEXT);
                G.bar(g, 130, ry + 6, 150, 7, v[i] / 100.0, v[i] >= 70 ? G.BLUE : G.ORANGE, G.TRACK);
                G.textR(g, v[i] + "%", 329, ry, G.b(13), G.INK);
            }
            y += 4 * 44 + 38;
        }

        // 합격자 후기
        String text = unlocked ? Data.REVIEW_FULL : Data.REVIEW_PREVIEW + "○○○○ ○○○ ○○○○ ○○○○…";
        int th = G.split(g, text, 296, G.f(12.5f)).size() * 20;
        double h = 50 + th + (unlocked ? 14 : 62);
        G.rr(g, 18.5, y, 325, h, 17, G.WHITE);
        G.rrLine(g, 18.5, y, 325, h, 17, new Color(0xFDE68A), 1.3f);
        double tw = G.tag(g, "인증 합격자", 34, y + 16, 22, G.b(11.5f), G.NAVY, G.WHITE, 8, 6);
        G.text(g, "[시기] 최종 합격 · 상세 후기", 34 + tw + 8, y + 18, G.f(12), G.SLATE);
        G.wrap(g, text, 34, y + 52, 296, G.f(12.5f), unlocked ? G.TEXT : new Color(0x9CA3AF), 20);
        if (!unlocked) {
            double by = y + 52 + th + 8;
            G.rr(g, 34, by, 294, 41, 11, G.YELLOW);
            G.textBox(g, "100P로 전체 후기 열기", 34, by, 294, 41, G.b(14), G.INK);
            b.on(34, by, 294, 41, () -> {
                if (App.points < 100) { App.toast("포인트가 부족해요. 후기를 쓰고 모아 보세요"); return; }
                App.points -= 100;
                unlocked = true;
                App.toast("100P를 사용해 후기를 열었어요");
            });
        }
        y += h + 14;
        b.height((int) y);
    }
}
