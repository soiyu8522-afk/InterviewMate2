package interviewmate;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 14. 면접 후기 공유하기 */
public class ReviewScreen extends Page {
    private final JTextField company = field("[회사명]");
    private final JTextField jobTime = field("백엔드 · [시기]");
    private final String[] stages = {"서류", "코딩 테스트", "1차 실무", "2차 임원", "인적성"};
    private final boolean[] stageOn = {true, true, true, false, false};
    private final List<String> questions = new ArrayList<>(Arrays.asList(
            "가장 큰 장애와 그때 내린 판단을 설명해 주세요.", "트랜잭션 격리 수준을 바꿔 본 경험이 있나요?"));
    private int result = 0;
    private boolean verify = true;
    private static final Color BROWN = new Color(0x92400E);

    public ReviewScreen() {
        super(G.BG, 90, App.H - 96, 900);
        place(company, 31, 38, 128, 26);
        place(jobTime, 190, 38, 128, 26);
        body.add(company);
        body.add(jobTime);
    }

    private static JTextField field(String text) {
        JTextField f = new JTextField(text);
        f.setBorder(null);
        f.setFont(G.f((float) (13.5 * App.S)));
        f.setForeground(G.TEXT);
        f.setBackground(G.WHITE);
        f.setCaretColor(G.BLUE);
        return f;
    }

    private int total() { return 50 + questions.size() * 10 + (verify ? 100 : 0); }

    @Override protected void draw(Graphics2D g) {
        App.header(g, this, "면접 후기 공유하기", "다녀온 면접 정보를 나누면 포인트를 드려요", G.INK, G.SUB);
        App.bigButton(g, this, "후기 올리고 " + total() + "P 받기", G.BLUE, () -> {
            if (company.getText().trim().isEmpty() || company.getText().equals("[회사명]")) {
                App.toast("회사 이름을 입력해 주세요");
                company.requestFocus();
                return;
            }
            int p = total();
            App.points += p;
            App.toast(p + "P를 받았어요! 소중한 후기 고마워요");
            App.back();
        });
    }

    @Override protected void drawBody(Graphics2D g, Body b) {
        G.text(g, "회사", 18, 6, G.b(13), G.INK);
        G.text(g, "직무 · 시기", 177, 6, G.b(13), G.INK);
        box(g, 18.5, 30, 151, 41);
        box(g, 177.5, 30, 152, 41);
        b.on(18.5, 30, 151, 41, company::requestFocus);
        b.on(177.5, 30, 152, 41, jobTime::requestFocus);

        G.text(g, "거친 단계", 18, 87, G.b(13), G.INK);
        double x = 18.5, y = 112;
        for (int i = 0; i < stages.length; i++) {
            int w = G.width(g, stages[i], G.b(13)) + 34;
            if (x + w > 345) { x = 18.5; y += 43; }
            SelectScreen.chipSmall(g, stages[i], x, y, w, stageOn[i]);
            final int k = i;
            b.on(x, y, w, 37, () -> stageOn[k] = !stageOn[k]);
            x += w + 6;
        }
        y += 57;

        // 받은 질문
        double h = 50 + questions.size() * 44 + 46;
        G.rr(g, 18.5, y, 325, h, 17, G.WHITE);
        G.text(g, "받은 질문", 32, y + 17, G.b(13), G.INK);
        G.textR(g, "질문 1개당 +10P", 330, y + 18, G.b(11.5f), BROWN);
        double qy = y + 47;
        for (int i = 0; i < questions.size(); i++) {
            G.rr(g, 32, qy, 298, 37, 10, new Color(0xF7F8FA));
            G.text(g, "Q" + (i + 1), 43, qy + 10, G.b(12), G.SLATE);
            String q = questions.get(i);
            while (G.width(g, q, G.f(12.5f)) > 230) q = q.substring(0, q.length() - 2) + "…";
            G.text(g, q, 68, qy + 10, G.f(12.5f), G.INK);
            G.textR(g, "×", 320, qy + 9, G.f(14), G.SUB);
            final int k = i;
            b.on(300, qy, 30, 37, () -> questions.remove(k));
            qy += 44;
        }
        G.dashed(g, 32, qy, 298, 39, 10, new Color(0x9CA3AF));
        G.textBox(g, "+ 질문 추가", 32, qy, 298, 39, G.b(13), G.TEXT);
        b.on(32, qy, 298, 39, () -> {
            String q = JOptionPane.showInputDialog(this, "받은 질문을 적어 주세요");
            if (q != null && !q.trim().isEmpty()) questions.add(q.trim());
        });
        y += h + 20;

        // 결과
        G.text(g, "결과", 18, y, G.b(13), G.INK);
        y += 26;
        G.rr(g, 18.5, y, 325, 44, 12, new Color(0xE5E7EB));
        String[] r = {"합격", "불합격", "결과 대기"};
        for (int i = 0; i < 3; i++) {
            double tx = 22.5 + i * 106;
            if (result == i) G.rr(g, tx, y + 4, 103, 36, 9, G.WHITE);
            G.textBox(g, r[i], tx, y + 4, 103, 36, G.b(13.5f), result == i ? G.INK : G.SLATE);
            final int k = i;
            b.on(tx, y, 103, 44, () -> result = k);
        }
        y += 58;

        // 인증
        G.rr(g, 18.5, y, 325, 98, 17, G.WHITE);
        G.checkbox(g, 32, y + 16, 20, verify);
        G.text(g, "면접 참여 인증하기 · +100P", 63, y + 14, G.b(13.5f), G.INK);
        G.wrap(g, "면접 안내 메일이나 문자 캡처를 올려 주세요. 이름·연락처는 자동으로 가려지고 인증 후 바로 삭제돼요.",
                63, y + 37, 262, G.f(12), G.SUB, 18);
        b.on(18.5, y, 325, 98, () -> verify = !verify);
        y += 112;

        // 받을 포인트
        G.rr(g, 18.5, y, 325, 128, 17, G.YELLOW_SOFT);
        G.text(g, "받을 포인트", 33, y + 16, G.b(12.5f), BROWN);
        String[][] rows = {{"기본 후기", "+50P"}, {"질문 " + questions.size() + "개", "+" + questions.size() * 10 + "P"},
                {"참여 인증", verify ? "+100P" : "+0P"}};
        for (int i = 0; i < 3; i++) {
            G.text(g, rows[i][0], 33, y + 44 + i * 24, G.f(12.5f), G.TEXT);
            G.textR(g, rows[i][1], 329, y + 44 + i * 24, G.b(12.5f), BROWN);
        }
        y += 142;
        b.height((int) y);
    }

    private void box(Graphics2D g, double x, double y, double w, double h) {
        G.rr(g, x, y, w, h, 12, G.WHITE);
        G.rrLine(g, x, y, w, h, 12, G.LINE, 1.2f);
    }
}
