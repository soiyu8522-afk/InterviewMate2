package interviewmate;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/** 3. 피드백 방식 선택 (2/2 단계) */
public class FeedbackModeScreen extends Canvas {
    public FeedbackModeScreen() {
        super(App.W, App.H);
        setBackground(G.BG);
        // 카메라 화면이 움직이도록 자주 다시 그림
        new javax.swing.Timer(60, e -> { if (isShowing()) repaint(); }).start();
    }

    @Override protected void draw(Graphics2D g) {
        G.back(g, 28, 58, G.INK);
        hit(10, 40, 44, 50, App::back);
        G.text(g, "2 / 2 단계", 59, 56, G.b(12.5f), G.SLATE);
        G.text(g, "피드백은 언제 받을까요?", 18, 94, G.b(21.5f), G.INK);
        G.text(g, "면접 중 내 모습은 화면 녹화로 분석해요", 18, 128, G.f(12.5f), G.SLATE);

        option(g, 161, true, "실시간 피드백", "면접 중",
                "시선·말 속도·자세를 화면 하단에 바로 알려줘요. 습관을 즉시 고치고 싶을 때 좋아요.");
        option(g, 265, false, "녹화 후 피드백", "면접 후",
                "면접에만 집중하고, 끝나면 녹화 영상과 함께 문항별 상세 리포트를 받아요.");

        // 카메라·마이크 확인 (실제 카메라 화면 + 마이크 소리 크기)
        G.rr(g, 18.5, 375, 325, 130, 15, G.WHITE);
        RoundRectangle2D cam = new RoundRectangle2D.Double(32, 388, 78, 104, 20, 20);
        g.setColor(new Color(0x1F2937));
        g.fill(cam);
        if (!Media.draw(g, cam, 32, 388, 78, 104)) {
            Shape old = g.getClip();
            g.clip(cam);
            Faces.silhouette(g, 71, 445, 14, new Color(0x4B5563));
            g.setClip(old);
        }
        G.text(g, "카메라·마이크 확인", 121, 396, G.b(14), G.INK);
        G.wrap(g, "얼굴과 어깨가 프레임 안에 들어오게 맞춰 주세요.", 121, 418, 205, G.f(12), G.SUB, 17);
        Color camCol = Media.camOk ? G.GREEN : G.ORANGE;
        if (Media.camOk) G.check(g, 123, 461, 9, camCol, 2f);
        else G.circle(g, 127, 465, 3.5, camCol);
        G.text(g, Media.camStatus, 137, 456, G.b(11.5f), camCol);
        G.mic(g, 127, 485, Media.micOk ? G.TEXT : G.SUB, false);
        if (Media.micOk) {
            G.bar(g, 140, 482, 150, 6, Math.min(1, Media.level * 1.4), Media.speaking() ? G.GREEN : G.BLUE_LIGHT, G.TRACK);
            G.text(g, Media.speaking() ? "잘 들려요" : "말해 보세요", 296, 477, G.f(10.5f), G.SUB);
        } else G.text(g, Media.micStatus, 140, 478, G.f(11.5f), G.ORANGE);

        // 설정 체크
        G.rr(g, 18.5, 520, 325, 153, 15, G.WHITE);
        row(g, 528, "질문을 음성으로 읽어주기", "면접관이 직접 말하듯 질문해요", App.tts, () -> App.tts = !App.tts);
        G.line(g, 33, 571, 329, 571, G.TRACK, 1);
        row(g, 577, "녹화 영상 저장", "다시 보기와 비교 리포트에 사용해요", App.saveVideo, () -> App.saveVideo = !App.saveVideo);
        G.line(g, 33, 620, 329, 620, G.TRACK, 1);
        row(g, 626, "질문 자막 표시", "음성과 함께 화면에 질문을 띄워요", App.subtitle, () -> App.subtitle = !App.subtitle);

        // 시작 버튼
        G.rr(g, 18.5, App.H - 80, 325, 50, 13, G.BLUE);
        String label = "녹화하며 면접 시작";
        int w = G.width(g, label, G.b(16));
        double x = App.W / 2.0 - (w + 18) / 2.0;
        G.circle(g, x + 4, App.H - 55, 4.5, G.WHITE);
        G.text(g, label, x + 18, App.H - 66, G.b(16), G.WHITE);
        hit(18.5, App.H - 80, 325, 50, () -> {
            ((InterviewScreen) App.screen("interview")).start();
            App.go("interview");
        });
    }

    private void option(Graphics2D g, double y, boolean isRealtime, String title, String tag, String desc) {
        boolean sel = App.realtime == isRealtime;
        if (sel) G.rr(g, 15.5, y - 3, 331, 101, 18, new Color(0xDCE4FB));
        G.rr(g, 18.5, y, 325, 95, 15, G.WHITE);
        if (sel) G.rrLine(g, 18.5, y, 325, 95, 15, G.BLUE, 2f);
        if (isRealtime) {
            G.rr(g, 35, y + 16, 41, 41, 10, G.ORANGE_SOFT);
            G.pulse(g, 45, y + 29, G.ORANGE);
        } else {
            G.rr(g, 35, y + 16, 41, 41, 10, G.BLUE_SOFT);
            G.monitor(g, 46, y + 27, G.BLUE);
        }
        int tw = G.text(g, title, 89, y + 17, G.b(16), G.INK);
        G.pill(g, tag, 89 + tw + 8, y + 19, 20, G.b(10.5f), new Color(0xF1F2F4), G.TEXT, 8);
        G.wrap(g, desc, 89, y + 45, 222, G.f(11.5f), G.SUB, 17.5);
        G.radio(g, 318, y + 25, 9.5, sel);
        hit(18.5, y, 325, 95, () -> App.realtime = isRealtime);
    }

    private void row(Graphics2D g, double y, String title, String sub, boolean on, Runnable toggle) {
        G.text(g, title, 33, y + 7, G.b(13.5f), G.INK);
        G.text(g, sub, 33, y + 27, G.f(11), G.SUB);
        G.checkbox(g, 306, y + 11, 20, on);
        hit(18.5, y, 325, 45, toggle);
    }
}
