package interviewmate;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/** 9. 주말 보스전 */
public class BossScreen extends Page {
    private int tab = 0;

    public BossScreen() { super(G.BG, 90, App.H - 118, 700); }

    @Override public void onShow() { scrollTop(); }

    @Override protected void draw(Graphics2D g) {
        App.header(g, this, "주말 보스전", null, G.INK, G.SUB);
        G.textR(g, "일요일 자정 종료", 343, 52, G.b(12), G.ORANGE);

        boolean can = App.bossTries > 0;
        G.rr(g, 18.5, App.H - 104, 325, 50, 13, can ? G.ORANGE : new Color(0x9CA3AF));
        G.textBox(g, can ? "보스전 도전하기" : "이번 주 도전을 모두 썼어요", 18.5, App.H - 104, 325, 50, G.b(16), G.WHITE);
        G.textC(g, "이번 주 남은 도전 " + App.bossTries + " / 3회", App.W / 2.0, App.H - 42, G.f(12.5f), G.SLATE);
        if (can) hit(18.5, App.H - 104, 325, 50, () -> {
            App.bossTries--;
            App.bossMode = true;
            App.customQuestions = null;
            ((InterviewScreen) App.screen("interview")).start();
            App.go("interview");
        });
    }

    @Override protected void drawBody(Graphics2D g, Body b) {
        double y = 6;
        RoundRectangle2D card = new RoundRectangle2D.Double(18.5, y, 325, 296, 36, 36);
        Shape old = g.getClip();
        g.setColor(new Color(0x141A27));
        g.fill(card);
        g.clip(card);
        G.rr(g, 18.5, y, 325, 140, 0, new Color(0x201D30));
        g.clip(new Rectangle2D.Double(18.5, y, 325, 140));
        Faces.person(g, 70, y + 85, 21, new Color(0x334155), Faces.GRAY_HAIR, Faces.Expr.FROWN, false, false, y + 140);
        Faces.person(g, 285, y + 85, 21, new Color(0x4A3A2C), Faces.HAIR, Faces.Expr.ANGRY, false, false, y + 140);
        Faces.person(g, 181, y + 66, 24, new Color(0x0B0F17), Faces.HAIR, Faces.Expr.ANGRY, false, false, y + 140);
        g.setClip(old);
        G.text(g, "이번 주 보스", 35, y + 156, G.b(11.5f), new Color(0xFDBA74));
        G.text(g, "깐깐한 임원 3인 패널", 35, y + 177, G.b(21), G.WHITE);
        G.wrap(g, "CEO·CTO·인사 임원이 번갈아 묻는 10문항 최종 면접. 돌발 카드가 반드시 2장 나와요.",
                35, y + 214, 292, G.f(12), new Color(0xD1D5DB), 19);
        G.text(g, "내 최고 설득 게이지", 35, y + 258, G.f(11.5f), new Color(0xD1D5DB));
        String best = App.bossBest + "%";
        int bw = G.width(g, " · 합격선 80%", G.f(11.5f));
        G.textR(g, " · 합격선 80%", 327, y + 258, G.f(11.5f), new Color(0xD1D5DB));
        G.textR(g, best, 327 - bw, y + 257, G.b(12), G.WHITE);
        G.bar(g, 35, y + 280, 292, 6, App.bossBest / 100.0, App.bossBest >= 80 ? G.GREEN_BRIGHT : G.ORANGE_BRIGHT, new Color(0x374151));
        G.line(g, 35 + 292 * 0.8, y + 276, 35 + 292 * 0.8, y + 290, G.WHITE, 1.5f);
        y += 310;

        // 탭
        G.rr(g, 18.5, y, 325, 44, 12, new Color(0xE5E7EB));
        String[] tabs = {"클리어 보상", "친구 랭킹"};
        for (int i = 0; i < 2; i++) {
            double tx = 22.5 + i * 159;
            if (tab == i) G.rr(g, tx, y + 4, 155, 36, 9, G.WHITE);
            G.textBox(g, tabs[i], tx, y + 4, 155, 36, G.b(14), tab == i ? G.INK : G.SLATE);
            final int k = i;
            b.on(tx, y, 155, 44, () -> tab = k);
        }
        y += 58;

        if (tab == 0) {
            String[][] r = {
                    {"배지", "‘최종 면접 통과’ 배지", "프로필에 표시되고 친구에게 공유할 수 있어요"},
                    {"해금", "새 면접관 ‘깐깐한 임원형’", "평소 연습에서도 임원 면접관을 고를 수 있어요"},
                    {"XP", "+500 XP", "첫 클리어 시 한 번 지급돼요"}};
            Color[] bg = {G.ORANGE_SOFT, G.BLUE_SOFT, G.GREEN_SOFT};
            Color[] fg = {G.ORANGE, G.BLUE, G.GREEN};
            for (int i = 0; i < 3; i++) {
                G.rr(g, 18.5, y, 325, 67, 15, G.WHITE);
                G.rr(g, 32, y + 13, 41, 41, 10, bg[i]);
                G.textBox(g, r[i][0], 32, y + 13, 41, 41, G.b(12.5f), fg[i]);
                G.text(g, r[i][1], 84, y + 13, G.b(13.5f), G.INK);
                G.text(g, r[i][2], 84, y + 37, G.f(11.5f), G.SUB);
                if (App.bossCleared) G.tag(g, "획득", 290, y + 22, 22, G.b(11), G.GREEN_SOFT, G.GREEN, 8, 11);
                y += 74.5;
            }
        } else {
            Object[][] rank = {{"민지", 91}, {"준호", 84}, {App.userName + " (나)", App.bossBest}, {"서연", 58}, {"태윤", 47}};
            java.util.Arrays.sort(rank, (a, c) -> (int) c[1] - (int) a[1]);
            G.rr(g, 18.5, y, 325, rank.length * 52 + 12, 15, G.WHITE);
            for (int i = 0; i < rank.length; i++) {
                double ry = y + 8 + i * 52;
                boolean me = ((String) rank[i][0]).contains("(나)");
                if (me) G.rr(g, 26, ry, 309, 46, 10, G.BLUE_SOFT);
                G.textBox(g, String.valueOf(i + 1), 34, ry, 24, 46, G.b(15), i < 3 ? G.ORANGE : G.SLATE);
                G.circle(g, 80, ry + 23, 14, new Color(0xE5E7EB));
                G.textBox(g, ((String) rank[i][0]).substring(0, 1), 66, ry + 9, 28, 28, G.b(12), G.TEXT);
                G.text(g, (String) rank[i][0], 104, ry + 14, G.b(13.5f), G.INK);
                int v = (int) rank[i][1];
                G.textR(g, v + "%", 322, ry + 14, G.b(14), v >= 80 ? G.GREEN : G.SLATE);
            }
            y += rank.length * 52 + 26;
        }
        b.height((int) y + 10);
    }
}
