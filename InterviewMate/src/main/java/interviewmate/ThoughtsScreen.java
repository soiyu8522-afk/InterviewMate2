package interviewmate;

import java.awt.*;
import java.awt.geom.*;

/** 7. 면접관 속마음 보기 */
public class ThoughtsScreen extends Page {
    private int sel = 2;
    private static final Color CARD = new Color(0x1C2230);

    public ThoughtsScreen() { super(G.DARK, 443, App.H, 420); }

    private static Color moodColor(int lvl) {
        if (lvl <= 1) return G.GREEN_BRIGHT; if (lvl == 2) return new Color(0xD1D5DB); return G.ORANGE_BRIGHT;
    }
    private static Faces.Expr moodFace(int lvl) {
        if (lvl == 0) return Faces.Expr.GRIN; if (lvl == 1) return Faces.Expr.SMILE; if (lvl == 2) return Faces.Expr.FLAT; return Faces.Expr.FROWN;
    }

    @Override protected void draw(Graphics2D g) {
        App.header(g, this, "면접관 속마음 보기", "면접관이 그 순간 무슨 생각을 했는지 들여다봐요", G.WHITE, new Color(0x9CA3AF));
        Data.Thought t = Data.THOUGHTS[sel];

        // 면접관 얼굴 + 말풍선
        RoundRectangle2D box = new RoundRectangle2D.Double(15, 93, 332, 214, 40, 40);
        g.setColor(CARD);
        g.fill(box);
        Shape old = g.getClip();
        g.clip(box);
        Faces.Expr e = moodFace(t.moodLevel);
        if (t.moodLevel == 3) e = Faces.Expr.FROWN;
        Faces.person(g, 181, 184, 37, new Color(0x1E293B), Faces.HAIR, e, false, false, 310);
        g.setClip(old);
        java.util.List<String> lines = G.split(g, t.text, 140, G.b(12.5f));
        double bh = lines.size() * 19 + 18;
        G.rr(g, 173, 110, 161, bh, 9, new Color(0xF8FAFC));
        for (int i = 0; i < lines.size(); i++) G.text(g, lines.get(i), 183, 118 + i * 19, G.b(12.5f), G.INK);
        G.tag(g, t.time, 26, 274, 22, G.b(11), new Color(0x0B0F17), G.WHITE, 8, 7);
        Color mc = moodColor(t.moodLevel);
        int mw = G.width(g, t.mood, G.b(11)) + 16;
        G.tag(g, t.mood, 336 - mw, 274, 22, G.b(11), t.moodLevel == 3 ? new Color(0x3A1A10) : new Color(0x14261A), mc, 8, 7);

        // 호감도 흐름 그래프
        G.rr(g, 15, 320, 332, 116, 20, CARD);
        G.text(g, "면접관 호감도 흐름", 28, 335, G.f(12), new Color(0xD1D5DB));
        int gw = G.width(g, "좋음", G.b(12));
        G.text(g, "좋음", 334 - gw, 335, G.b(12), G.GREEN_BRIGHT);
        G.textR(g, "첫인상 7초 · ", 334 - gw, 335, G.f(12), new Color(0xD1D5DB));
        g.setColor(new Color(0x4B5563));
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1f, new float[]{3, 3}, 0));
        g.draw(new Line2D.Double(28, 390, 330, 390));
        double[] xs = {56, 121, 167, 241, 316};
        double[] ys = new double[5];
        for (int i = 0; i < 5; i++) ys[i] = new double[]{368, 377, 389, 410}[Data.THOUGHTS[i].moodLevel];
        Path2D p = new Path2D.Double();
        p.moveTo(28, 385);
        double px = 28, py = 385;
        for (int i = 0; i <= 5; i++) {
            double nx = i < 5 ? xs[i] : 330, ny = i < 5 ? ys[i] : 368;
            double mx = (px + nx) / 2;
            p.curveTo(mx, py, mx, ny, nx, ny);
            px = nx; py = ny;
        }
        g.setColor(G.BLUE_LIGHT);
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(p);
        for (int i = 0; i < 5; i++) {
            Color c = moodColor(Data.THOUGHTS[i].moodLevel);
            if (i == sel) { G.circle(g, xs[i], ys[i], 7.5, CARD); G.circle(g, xs[i], ys[i], 6.5, c); }
            else { G.circle(g, xs[i], ys[i], 5, CARD); G.circle(g, xs[i], ys[i], 4, c); }
            final int k = i;
            hit(xs[i] - 12, ys[i] - 12, 24, 24, () -> sel = k);
        }
    }

    @Override protected void drawBody(Graphics2D g, Body b) {
        double y = 7;
        for (int i = 0; i < Data.THOUGHTS.length; i++) {
            Data.Thought t = Data.THOUGHTS[i];
            java.util.List<String> lines = G.split(g, t.text, 255, G.b(13));
            double h = lines.size() * 20 + 40;
            boolean on = i == sel;
            G.rr(g, 15, y, 332, h, 15, on ? new Color(0x1F2840) : CARD);
            if (on) G.rrLine(g, 15, y, 332, h, 15, G.BLUE_LIGHT, 1.2f);
            G.text(g, t.time, 26, y + 14, G.b(11.5f), new Color(0x9CA3AF));
            for (int k = 0; k < lines.size(); k++) G.text(g, lines.get(k), 68, y + 12 + k * 20, G.b(13), G.WHITE);
            Color mc = t.moodLevel == 2 ? new Color(0xD1D5DB) : moodColor(t.moodLevel);
            if (t.moodLevel == 3) mc = G.ORANGE_BRIGHT;
            G.text(g, t.mood + " · " + t.reason, 68, y + 14 + lines.size() * 20, G.f(11.5f), mc);
            final int k = i;
            b.on(15, y, 332, h, () -> sel = k);
            y += h + 8;
        }
        b.height((int) y + 10);
    }
}
