package interviewmate;

import javax.swing.*;
import java.awt.*;

/**
 * 고정 영역(상단 제목·하단 버튼) + 가운데 스크롤 영역으로 이루어진 화면의 뼈대.
 * draw()      : 고정 영역 그리기
 * drawBody()  : 스크롤 영역 그리기 (좌표는 스크롤 영역 기준)
 */
public abstract class Page extends Canvas {
    protected final Body body;
    protected final JScrollPane sp;

    protected Page(Color bg, int top, int bottom, int bodyH) {
        super(App.W, App.H);
        setBackground(bg);
        body = new Body(bodyH, bg);
        sp = scroll(body, bg);
        place(sp, 0, top, App.W, bottom - top);
        add(sp);
    }

    protected abstract void drawBody(Graphics2D g, Body b);

    protected void scrollTop() { SwingUtilities.invokeLater(() -> sp.getVerticalScrollBar().setValue(0)); }

    public class Body extends Canvas {
        Body(int h, Color bg) { super(App.W, h); setBackground(bg); }
        @Override protected void draw(Graphics2D g) { drawBody(g, this); }
        public void on(double x, double y, double w, double h, Runnable r) { hit(x, y, w, h, r); }
        public void height(int h) { setHeight(h); }
    }
}
