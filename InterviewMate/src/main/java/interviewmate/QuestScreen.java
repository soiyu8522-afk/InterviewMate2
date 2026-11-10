package interviewmate;

import java.awt.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 6. 데일리 퀘스트 */
public class QuestScreen extends Page {
    public QuestScreen() {
        super(G.BG, 0, App.H - 84, 790);
        new javax.swing.Timer(1000, e -> { if (isShowing()) body.repaint(); }).start();
    }

    @Override public void onShow() { scrollTop(); }

    @Override protected void draw(Graphics2D g) { App.navBar(g, this, 2); }

    @Override protected void drawBody(Graphics2D g, Body b) {
        G.text(g, "매일 자정에 새로 고쳐져요", 18, 53, G.b(12), G.ORANGE);
        G.text(g, "데일리 퀘스트", 18, 76, G.b(25), G.INK);
        Duration left = Duration.between(LocalDateTime.now(), LocalDate.now().plusDays(1).atStartOfDay());
        G.textR(g, String.format("남은 시간 %02d:%02d", left.toHours(), left.toMinutes() % 60), 343, 92, G.f(13), G.SUB);

        // 연속 기록 카드
        G.rr(g, 18.5, 125, 325, 186, 18, G.NAVY);
        G.rr(g, 36, 143, 41, 41, 10, G.ORANGE);
        G.flame(g, 56.5, 165, 7.5, G.WHITE);
        G.text(g, App.streak + "일 연속 연습 중", 86, 141, G.b(19), G.WHITE);
        G.text(g, "최고 기록 " + App.bestStreak + "일", 86, 170, G.f(12), new Color(0xD1D5DB));
        G.rr(g, 271, 148, 48, 30, 15, new Color(0x1F2937));
        G.textBox(g, "Lv." + App.level, 271, 148, 48, 30, G.b(12.5f), new Color(0xFDBA74));
        String[] days = {"월", "화", "수", "목", "금", "토", "일"};
        int today = LocalDate.now().getDayOfWeek().getValue() - 1;
        for (int i = 0; i < 7; i++) {
            double cx = 53 + i * 42;
            G.textC(g, days[i], cx, 197, G.f(11.5f), new Color(0xD1D5DB));
            if (App.weekDone[i]) {
                G.circle(g, cx, 235, 15.5, G.ORANGE);
                G.check(g, cx - 6, 230, 12, G.WHITE, 2.2f);
            } else if (i == today) G.ring(g, cx, 235, 14.5, G.ORANGE_BRIGHT, 1.8f);
            else G.ring(g, cx, 235, 14.5, new Color(0x374151), 1.8f);
        }
        G.text(g, "다음 레벨까지", 36, 263, G.f(12), new Color(0xD1D5DB));
        G.textR(g, App.xp + " / 500 XP", 325, 263, G.f(12), new Color(0xD1D5DB));
        G.bar(g, 36, 286, 277 + 12, 7, App.xp / 500.0, G.ORANGE_BRIGHT, new Color(0x374151));

        G.text(g, "오늘의 퀘스트", 18, 326, G.b(15), G.INK);
        double y = 357;
        for (Data.Quest q : App.quests) {
            G.rr(g, 18.5, y, 325, 67, 15, G.WHITE);
            G.rr(g, 32, y + 13, 41, 41, 10, q.tagBg);
            G.textBox(g, q.tag, 32, y + 13, 41, 41, G.b(12.5f), q.tagFg);
            G.text(g, q.title, 84, y + 13, G.b(13.5f), G.INK);
            G.bar(g, 84, y + 43, 125, 6, q.now / (double) q.goal, G.ORANGE, G.TRACK);
            G.text(g, q.now + "/" + q.goal, 217, y + 38, G.f(11.5f), G.SUB);
            double bx = 246, by = y + 15, bw = 83, bh = 37;
            if (q.claimed) {
                G.rr(g, bx, by, bw, bh, 11, new Color(0xF1F2F4));
                G.textBox(g, "받음", bx, by, bw, bh, G.b(13), G.SLATE);
            } else if (q.done()) {
                G.rr(g, bx, by, bw, bh, 11, G.ORANGE);
                G.textBox(g, "+" + q.reward + " XP 받기", bx, by, bw, bh, G.b(12), G.WHITE);
                b.on(bx, by, bw, bh, () -> {
                    q.claimed = true;
                    App.addXp(q.reward);
                    App.toast("+" + q.reward + " XP를 받았어요");
                });
            } else {
                G.rr(g, bx, by, bw, bh, 11, G.WHITE);
                G.rrLine(g, bx, by, bw, bh, 11, G.LINE, 1.2f);
                G.textBox(g, "+" + q.reward + " XP", bx, by, bw, bh, G.b(12.5f), G.INK);
                b.on(18.5, y, 325, 67, () -> {
                    if (q.id.equals("press")) App.interviewer = 1;
                    App.customQuestions = null;
                    App.bossMode = false;
                    App.go("select");
                });
            }
            y += 76.5;
        }

        // 주간 챌린지
        y += 4;
        G.rr(g, 18.5, y, 325, 74, 15, G.BLUE_SOFT);
        G.rr(g, 32, y + 16, 41, 41, 10, G.BLUE);
        G.flag(g, 47, y + 27, 17, G.WHITE, 2f);
        G.text(g, "주간 챌린지 · 5일 연습 (" + App.weeklyDays + "/5)", 84, y + 16, G.b(13.5f), G.INK);
        G.text(g, App.executiveUnlocked ? "깐깐한 임원형 면접관이 열려 있어요" : "완료하면 새 면접관 ‘깐깐한 임원형’이 열려요",
                84, y + 40, G.f(11.5f), G.SLATE);
        b.on(18.5, y, 325, 74, () -> App.go("boss"));
        b.height((int) y + 96);
    }
}
