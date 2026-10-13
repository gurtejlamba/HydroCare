package com.hydrocare.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import java.time.DayOfWeek;
import java.time.LocalDate;

public class HydroChartView extends View {

    private final Paint metPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint notMetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bgPaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint goalPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final int[]    intakes = new int[7];
    private final int[]    goals   = new int[7];
    private final String[] labels  = new String[7];

    private static final String[] DAY_NAMES = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    public HydroChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        float d = getResources().getDisplayMetrics().density;

        metPaint.setColor(0xFF1565C0);
        notMetPaint.setColor(0xFF64B5F6);
        bgPaint.setColor(0xFFE3F2FD);

        goalPaint.setColor(0xFFFF7043);
        goalPaint.setStyle(Paint.Style.STROKE);
        goalPaint.setStrokeWidth(2.5f * d);
        goalPaint.setPathEffect(new DashPathEffect(new float[]{10 * d, 5 * d}, 0));

        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(11 * d);
    }

    public void loadData(SharedPreferences prefs) {
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate date = today.minusDays(6 - i);
            String key = date.toString();
            intakes[i] = (i == 6)
                    ? prefs.getInt("intake", 0)
                    : prefs.getInt("intake_" + key, 0);
            goals[i]  = prefs.getInt("goal_" + key, 2000);
            DayOfWeek dow = date.getDayOfWeek();
            labels[i] = DAY_NAMES[dow.getValue() % 7];
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth(), h = getHeight();
        if (w == 0 || h == 0) return;

        float d       = getResources().getDisplayMetrics().density;
        int labelH    = (int) (labelPaint.getTextSize() * 2.2f);
        int topPad    = (int) (6 * d);
        int chartH    = h - labelH - topPad;
        int colW      = w / 7;
        int barW      = (int) (colW * 0.55f);
        int barOff    = (colW - barW) / 2;
        int maxVal    = 3500;
        float radius  = 6 * d;

        for (int i = 0; i < 7; i++) {
            int cx     = i * colW + colW / 2;
            int left   = i * colW + barOff;
            int right  = left + barW;
            int bottom = topPad + chartH;

            canvas.drawRoundRect(new RectF(left, topPad, right, bottom), radius, radius, bgPaint);

            int intake = intakes[i];
            int goal   = goals[i] > 0 ? goals[i] : 2000;
            int fillH  = (int) (chartH * Math.min((float) intake / maxVal, 1f));
            if (fillH > 0) {
                Paint fill = intake >= goal ? metPaint : notMetPaint;
                canvas.drawRoundRect(new RectF(left, bottom - fillH, right, bottom), radius, radius, fill);
            }

            float goalY = topPad + chartH * (1f - Math.min((float) goal / maxVal, 1f));
            canvas.drawLine(left, goalY, right, goalY, goalPaint);

            boolean isToday = (i == 6);
            labelPaint.setColor(isToday ? 0xFF1565C0 : 0xFF888888);
            labelPaint.setFakeBoldText(isToday);
            canvas.drawText(labels[i], cx, h - (int) (labelPaint.getTextSize() * 0.25f), labelPaint);
        }
    }
}
