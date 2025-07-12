package hk.jud.app.lyo.util;

import java.util.Calendar;
import java.util.Date;

public class ChineseDate {
    private static final String[] CHINESE_NUMBERS = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
    private static final String[] CHINESE_UNITS = {"", "十", "百", "千"};

    public static String toChineseDate(Date date) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return convertYearToChinese(year) + "年" + convertToChinese(month) + "月" + convertToChinese(day) + "日";
    }

    private static String convertToChinese(int number) {
        if (number == 0) return CHINESE_NUMBERS[0];
        
        if (number < 10) return CHINESE_NUMBERS[number];
        if (number == 10) return "十";
        if (number < 20) return "十" + CHINESE_NUMBERS[number % 10];
        
        StringBuilder result = new StringBuilder();
        char[] digits = String.valueOf(number).toCharArray();
        int len = digits.length;

        for (int i = 0; i < len; i++) {
            int digit = digits[i] - '0';
            int unitIndex = len - i - 1;

            if (digit != 0) {
                result.append(CHINESE_NUMBERS[digit]).append(CHINESE_UNITS[unitIndex]);
            } else if (i < len - 1 && digits[i + 1] != '0') {
                result.append(CHINESE_NUMBERS[0]);
            }
        }

        return result.toString();
    }

    private static String convertYearToChinese(int year) {
        StringBuilder result = new StringBuilder();
        for (char digit : String.valueOf(year).toCharArray()) {
            result.append(CHINESE_NUMBERS[digit - '0']);
        }
        return result.toString();
    }

    public static void main(String[] args) {
        Date dt = new Date(2025 - 1900, 5, 11); // June 11, 2025
        String content = "[EMAIL_DATE]";
        content = content.replace("[EMAIL_DATE]", toChineseDate(dt));
        System.out.println(content); // Outputs: 二零二五年六月十一日
    }
}