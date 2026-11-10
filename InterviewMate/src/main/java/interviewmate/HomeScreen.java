package interviewmate;

import java.awt.*;
import java.time.LocalTime;

/** 1. 홈 */
public class HomeScreen extends Canvas {
    private final Body body = new Body();

    public HomeScreen() {
        super(App.W, App.H);
        setBackground(G.BG);
        javax.swing.JScrollPane sp = scroll(body, G.BG);
        place(sp, 0, 0, App.W, App.H - 84);
        add(sp);
    }

    @Override public void onShow() { body.repaint(); repaint(); }

    @Override protected void draw(Graphics2D g) { App.navBar(g, this, 0); }

    private static class Body extends Canvas {
        Body() { super(App.W, 1010); setBackground(G.BG); }

        @Override protected void draw(Graphics2D g) {
            int h = LocalTime.now().getHour();
            String hello = h < 12 ? "좋은 아침이에요" : h < 18 ? "좋은 오후예요" : "좋은 저녁이에요";
            G.text(g, App.userName + "님, " + hello, 19, 54, G.f(13), G.SUB);

            // 연속 기록 배지
            G.rr(g, 258, 52, 86, 33, 16.5, G.ORANGE_SOFT);
            G.flame(g, 277, 69, 6.5, G.ORANGE);
            G.text(g, App.streak + "일 연속", 290, 59, G.b(12.5f), G.ORANGE);
            hit(258, 52, 86, 33, () -> App.tab("quest"));

            G.text(g, "오늘도 한 문항씩", 18, 77, G.b(25), G.INK);
            G.text(g, "단단하게", 18, 112, G.b(25), G.INK);

            // 면접 시작 카드
            G.rr(g, 18.5, 165, 325, 155, 17, G.BLUE);
            G.pill(g, "모의 면접", 39, 186, 26, G.b(11.5f), new Color(255, 255, 255, 55), G.WHITE, 9);
            G.arrowRight(g, 306, 199, 15, G.WHITE, 2.2f);
            G.text(g, "면접 시작하기", 39, 225, G.b(21), G.WHITE);
            G.wrap(g, "면접관 성향을 고르면 AI 면접관이 직접 질문해요. 내 모습은 녹화해서 피드백해 드려요.",
                    39, 262, 285, G.f(12.5f), new Color(0xDCE5FF), 20);
            hit(18.5, 165, 325, 155, () -> { App.customQuestions = null; App.bossMode = false; App.go("select"); });

            // 오늘의 퀘스트
            G.rr(g, 18.5, 339, 325, 194, 17, G.WHITE);
            G.text(g, "오늘의 퀘스트", 35, 363, G.b(15), G.INK);
            G.textR(g, "모두 보기", 327, 365, G.b(12), G.BLUE);
            hit(260, 355, 80, 30, () -> App.tab("quest"));
            Data.Quest[] list = {App.quest("intro"), App.quest("press"), App.quest("gaze")};
            int done = 0;
            for (Data.Quest q : list) if (q.done()) done++;
            G.bar(g, 35, 411, 257, 7, done / 3.0, G.ORANGE, G.TRACK);
            G.textR(g, done + " / 3", 327, 405, G.b(12), G.SLATE);
            for (int i = 0; i < 3; i++) {
                Data.Quest q = list[i];
                double cy = 447 + i * 30;
                if (q.done()) {
                    G.circle(g, 45, cy, 10, G.ORANGE);
                    G.check(g, 40, cy - 4, 10, G.WHITE, 2f);
                    int w = G.text(g, q.title, 65, cy - 9, G.f(13), G.SUB);
                    G.line(g, 65, cy + 1, 65 + w, cy + 1, G.SUB, 1f);
                } else {
                    G.ring(g, 45, cy, 9.5, G.LINE, 1.6f);
                    G.text(g, q.title, 65, cy - 9, G.f(13), G.INK);
                }
                G.textR(g, "+" + q.reward + " XP", 327, cy - 8, G.b(11), G.ORANGE);
            }

            // 최근 면접
            G.text(g, "최근 면접", 18, 560, G.b(15), G.INK);
            G.textR(g, "리포트 보기", 343, 562, G.b(12), G.BLUE);
            hit(270, 552, 80, 30, () -> { App.lastSession = App.sessions.get(0); App.tab("report"); });
            double y = 599;
            for (int i = 0; i < Math.min(3, App.sessions.size()); i++) {
                Data.Session s = App.sessions.get(i);
                Data.Type t = Data.TYPES[s.type];
                G.rr(g, 18.5, y, 325, 67, 15, G.WHITE);
                G.rr(g, 32, y + 13, 41, 41, 10, t.tagBg);
                G.textBox(g, t.short1, 32, y + 13, 41, 41, G.b(14), t.tagFg);
                G.text(g, t.name + " · " + s.count + "문항", 84, y + 13, G.b(14), G.INK);
                G.text(g, s.when + " · " + s.modeName(), 84, y + 38, G.f(11.5f), G.SUB);
                G.textR(g, s.total() + "점", 330, y + 21, G.b(17), G.BLUE);
                final Data.Session ss = s;
                hit(18.5, y, 325, 67, () -> { App.lastSession = ss; App.go("report"); });
                y += 76;
            }

            // 더 많은 연습 (목업의 나머지 화면으로 가는 입구)
            y += 14;
            G.text(g, "더 많은 연습", 18, y, G.b(15), G.INK);
            y += 34;
            String[][] tiles = {
                    {"주말 보스전", "깐깐한 임원 3인 패널", "boss"},
                    {"채용공고 맞춤 면접", "공고로 예상 질문 만들기", "posting"},
                    {"자기소개서 피드백", "고칠 문장 · 살릴 문장", "resume"},
                    {"기업 면접 정보", "합격자 후기 · 면접 과정", "company"}};
            Color[] bg = {G.ORANGE_SOFT, G.BLUE_SOFT, G.GREEN_SOFT, G.YELLOW_SOFT};
            Color[] fg = {G.ORANGE, G.BLUE, G.GREEN, new Color(0xB45309)};
            for (int i = 0; i < 4; i++) {
                double tx = 18.5 + (i % 2) * 166, ty = y + (i / 2) * 86;
                G.rr(g, tx, ty, 159, 76, 15, G.WHITE);
                G.circle(g, tx + 18, ty + 18, 9, bg[i]);
                G.circle(g, tx + 18, ty + 18, 4, fg[i]);
                G.text(g, tiles[i][0], tx + 14, ty + 33, G.b(13), G.INK);
                G.text(g, tiles[i][1], tx + 14, ty + 52, G.f(11), G.SUB);
                final String dest = tiles[i][2];
                hit(tx, ty, 159, 76, () -> App.go(dest));
            }
            setHeight((int) (y + 190));
        }
    }
}
