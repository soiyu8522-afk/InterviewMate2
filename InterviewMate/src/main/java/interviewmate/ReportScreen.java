package interviewmate;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/** 5. 면접 리포트 */
public class ReportScreen extends Page {
    private int selQ = 2;
    private Data.Session shown;

    public ReportScreen() { super(G.BG, 90, App.H - 96, 900); }

    @Override public void onShow() {
        if (shown != App.lastSession) {
            shown = App.lastSession;
            selQ = Math.min(2, shown.fb.size() - 1);
            scrollTop();
        }
    }

    @Override protected void draw(Graphics2D g) {
        Data.Session s = App.lastSession;
        App.header(g, this, "면접 리포트",
                Data.TYPES[s.type].name + " · " + s.count + "문항 · " + s.modeName(), G.INK, G.SUB);
        G.tag(g, "예시 데이터", 281, 54, 22, G.b(11), new Color(0xE5E7EB), G.TEXT, 8, 8);

        Data.Quest q = App.quest("mock");
        if (q.done() && !q.claimed) {
            G.rr(g, 18.5, App.H - 80, 325, 50, 13, G.ORANGE);
            String label = "퀘스트 보상 받기 · +" + q.reward + " XP";
            int w = G.width(g, label, G.b(16));
            double x = App.W / 2.0 - (w + 22) / 2.0;
            G.flag(g, x + 2, App.H - 64, 17, G.WHITE, 2f);
            G.text(g, label, x + 22, App.H - 66, G.b(16), G.WHITE);
            hit(18.5, App.H - 80, 325, 50, () -> {
                q.claimed = true;
                App.addXp(q.reward);
                App.toast("+" + q.reward + " XP를 받았어요");
            });
        } else {
            App.bigButton(g, this, "같은 설정으로 다시 연습하기", G.BLUE, () -> {
                App.interviewer = s.type;
                App.realtime = s.realtime;
                App.customQuestions = null;
                App.questionCount = s.count == 3 || s.count == 10 ? s.count : 5;
                ((InterviewScreen) App.screen("interview")).start();
                App.go("interview");
            });
        }
    }

    @Override protected void drawBody(Graphics2D g, Body b) {
        Data.Session s = App.lastSession;
        int n = s.fb.size();
        double y = 7;

        // 녹화 영상
        G.rr(g, 18.5, y, 325, 238, 18, G.NAVY);
        RoundRectangle2D vid = new RoundRectangle2D.Double(30, y + 11, 301, 163, 20, 20);
        g.setColor(new Color(0x232B3B));
        g.fill(vid);
        Shape old = g.getClip();
        g.clip(vid);
        if (!Media.draw(g, s.snap, vid, 30, y + 11, 301, 163)) Faces.silhouette(g, 180.5, y + 92, 30, new Color(0x4B5563));
        g.setClip(old);
        G.circle(g, 175, y + 92, 27, new Color(0xF3F4F6));
        G.play(g, 177, y + 92, 15, G.INK);
        b.on(148, y + 65, 54, 54, () -> App.toast(App.saveVideo ? "녹화 영상 재생은 예시 화면이에요" : "영상 저장을 꺼서 다시 볼 수 없어요"));
        String stamp = stamp(selQ) + " · Q" + (selQ + 1);
        G.tag(g, stamp, 39, y + 20, 21, G.b(11), new Color(0x111827), G.WHITE, 7, 6);

        // 타임라인
        double ty = y + 193;
        G.rr(g, 30, ty - 2, 301, 4, 2, new Color(0x374151));
        int dots = Math.min(5, n);
        double step = 301.0 / Math.max(1, dots);
        double fill = 30 + step * (selQ + 0.5);
        G.rr(g, 30, ty - 2, fill - 30, 4, 2, G.BLUE_LIGHT);
        for (int i = 0; i < dots; i++) {
            double dx = 30 + step * (i + 0.5);
            boolean good = goodMoment(s, i);
            G.circle(g, dx, ty, i == selQ ? 8 : 6.5, G.NAVY);
            G.circle(g, dx, ty, i == selQ ? 6.5 : 5, good ? new Color(0x60A5FA) : G.ORANGE_BRIGHT);
            final int k = i;
            b.on(dx - 12, ty - 12, 24, 24, () -> selQ = k);
        }
        G.circle(g, 34, y + 216, 5, G.ORANGE_BRIGHT);
        G.text(g, "개선 포인트", 44, y + 208, G.f(11.5f), new Color(0xD1D5DB));
        G.circle(g, 114, y + 216, 5, new Color(0x60A5FA));
        G.text(g, "잘한 장면", 124, y + 208, G.f(11.5f), new Color(0xD1D5DB));
        y += 252;

        // 태도 분석
        G.rr(g, 18.5, y, 325, 208, 18, G.WHITE);
        G.text(g, "태도 분석", 33, y + 21, G.b(15), G.INK);
        String total = String.valueOf(s.total());
        G.textR(g, "/ 100", 329, y + 22, G.f(13), G.SUB);
        int tw = G.width(g, total, G.b(22));
        G.text(g, total, 329 - G.width(g, "/ 100", G.f(13)) - 5 - tw, y + 12, G.b(22), G.BLUE);
        G.textR(g, "종합", 329 - G.width(g, "/ 100", G.f(13)) - 10 - tw, y + 22, G.f(13), G.SUB);
        for (int i = 0; i < 5; i++) {
            double ry = y + 65 + i * 29.7;
            int v = s.scores[i];
            G.text(g, Data.SCORE_NAMES[i], 33, ry - 9, G.f(13), G.TEXT);
            G.bar(g, 110, ry - 3.5, 169, 7, v / 100.0, v < 70 ? G.ORANGE : G.BLUE, G.TRACK);
            G.textR(g, String.valueOf(v), 314, ry - 9, G.b(13), G.INK);
        }
        y += 222;

        // 문항별 피드백 (카드 높이를 먼저 계산)
        double cardTop = y;
        Data.QFeedback f = s.fb.get(selQ);
        double cardH = 102 + G.split(g, "“" + f.q + "”", 296, G.b(14)).size() * 21 + 10
                + boxH(g, f.good) + 8 + boxH(g, f.fix) + 16;
        G.rr(g, 18.5, cardTop, 325, cardH, 18, G.WHITE);
        G.text(g, "문항별 피드백", 33, y + 20, G.b(15), G.INK);
        y += 50;
        int cols = Math.min(5, n);
        double tw2 = (296 - (cols - 1) * 6) / (double) cols;
        int start = Math.max(0, Math.min(selQ - 2, n - cols));
        for (int i = 0; i < cols; i++) {
            int qi = start + i;
            double tx = 33 + i * (tw2 + 6);
            boolean on = qi == selQ;
            if (on) G.rr(g, tx, y, tw2, 37, 10, G.INK);
            else { G.rr(g, tx, y, tw2, 37, 10, G.WHITE); G.rrLine(g, tx, y, tw2, 37, 10, G.LINE, 1.2f); }
            G.textBox(g, "Q" + (qi + 1), tx, y, tw2, 37, G.b(13.5f), on ? G.WHITE : G.TEXT);
            b.on(tx, y, tw2, 37, () -> selQ = qi);
        }
        y += 52;
        y += G.wrap(g, "“" + f.q + "”", 33, y, 296, G.b(14), G.INK, 21) + 10;
        y = box(g, y, "잘한 점", f.good, G.GREEN_SOFT, G.GREEN);
        box(g, y + 8, "고칠 점", f.fix, G.CREAM, G.ORANGE);
        y = cardTop + cardH + 14;

        // 면접관 속마음 입구
        G.rr(g, 18.5, y, 325, 66, 18, G.NAVY);
        Faces.person(g, 50, y + 31, 13, new Color(0x334155), Faces.HAIR, Faces.Expr.FROWN, false, false, y + 66);
        G.text(g, "면접관 속마음 보기", 78, y + 14, G.b(14), G.WHITE);
        G.text(g, "그 순간 면접관은 무슨 생각을 했을까요?", 78, y + 36, G.f(11.5f), new Color(0x9CA3AF));
        G.arrowRight(g, 312, y + 33, 12, G.WHITE, 2f);
        b.on(18.5, y, 325, 66, () -> App.go("thoughts"));
        y += 84;
        b.height((int) y);
    }

    private double boxH(Graphics2D g, String text) {
        return Math.max(42, G.split(g, text, 230, G.f(12.5f)).size() * 19 + 22);
    }

    private double box(Graphics2D g, double y, String title, String text, Color bg, Color fg) {
        java.util.List<String> lines = G.split(g, text, 230, G.f(12.5f));
        double h = boxH(g, text);
        G.rr(g, 33, y, 296, h, 11, bg);
        G.text(g, title, 45, y + 11, G.b(12), fg);
        for (int i = 0; i < lines.size(); i++) G.text(g, lines.get(i), 92, y + 11 + i * 19, G.f(12.5f), G.TEXT);
        return y + h;
    }

    private boolean goodMoment(Data.Session s, int i) {
        return (s.scores[(i + 1) % 5] + i * 7) % 2 == 0 || i == 0;
    }

    private String stamp(int q) {
        int sec = 31 + q * 120 + (q == 2 ? 0 : q * 7);
        if (q == 2) sec = 271;
        return String.format("%02d:%02d", sec / 60, sec % 60);
    }
}
