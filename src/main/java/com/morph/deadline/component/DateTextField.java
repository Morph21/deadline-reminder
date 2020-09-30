package com.morph.deadline.component;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class DateTextField extends JTextField {

    private String DEFAULT_DATE_FORMAT = "MM/dd/yyyy";
    private static final int DIALOG_WIDTH = 300;
    private static final int DIALOG_HEIGHT = 200;

    int currentYear;
    int currentMonth;
    int currentday;

    private SimpleDateFormat dateFormat;
    private DatePanel datePanel = null;
    private JDialog dateDialog = null;
    private Color weekendFontColor = Color.red;
    private Color currentDayBackgroundColor = Color.gray;
    private Color defaultBackground = null;

    public DateTextField() {
        this(new Date());
    }

    public DateTextField(String dateFormatPattern, Date date) {
        init(dateFormatPattern, date);
    }

    public DateTextField(Date date) {
        init(date);
    }

    public void initColors(Color weekendFontColor, Color currentDayBackgroundColor) {
        this.weekendFontColor = weekendFontColor;
        this.currentDayBackgroundColor = currentDayBackgroundColor;
    }

    private void init(String dateFormatPattern, Date date) {
        DEFAULT_DATE_FORMAT = dateFormatPattern;
        init(date);
    }

    private void init(Date date) {
        setDate(date);
        setEditable(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addListeners();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        this.currentYear = cal.get(Calendar.YEAR);
        this.currentMonth = cal.get(Calendar.MONTH) + 1;
        this.currentday = cal.get(Calendar.DAY_OF_MONTH);
    }

    private void addListeners() {
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent paramMouseEvent) {
                if (datePanel == null) {
                    datePanel = new DatePanel();
                }
                Point point = getLocationOnScreen();
                point.y = point.y + 30;
                showDateDialog(datePanel, point);
            }
        });

    }

    private void showDateDialog(DatePanel dateChooser, Point position) {
        Window owner = SwingUtilities
                .getWindowAncestor(DateTextField.this);
        while (!(owner instanceof JFrame)) {
            owner = owner.getOwner();
        }
        JFrame dialogsOwner = (JFrame) owner;
        if (dateDialog == null || dateDialog.getOwner() != owner) {
            dateDialog = createDateDialog(dialogsOwner, dateChooser);
        }
        addDateDialogListeners();
        dateDialog.setLocation(getAppropriateLocation(dialogsOwner, position));
        dateDialog.setVisible(true);
    }

    private void addDateDialogListeners() {
        dateDialog.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() ==KeyEvent.VK_ESCAPE) {
                    dateDialog.setVisible(false);
                }
            }
        });
    }

    private JDialog createDateDialog(JFrame owner, JPanel contentPanel) {
        JDialog dialog = new JDialog(owner, "Date Selected", true);
        dialog.setUndecorated(true);
        dialog.getContentPane().add(contentPanel, BorderLayout.CENTER);
        dialog.pack();
        dialog.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        return dialog;
    }

    private Point getAppropriateLocation(Frame owner, Point position) {
        Point result = new Point(position);
        Point p = owner.getLocation();
        int offsetX = (position.x + DIALOG_WIDTH) - (p.x + owner.getWidth());
        int offsetY = (position.y + DIALOG_HEIGHT) - (p.y + owner.getHeight());

        if (offsetX > 0) {
            result.x -= offsetX;
        }

        if (offsetY > 0) {
            result.y -= offsetY;
        }

        return result;
    }

    private SimpleDateFormat getDefaultDateFormat() {
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
        }
        return dateFormat;
    }

    public void setText(Date date) {
        setDate(date);
    }

    public void setDate(Date date) {
        super.setText(getDefaultDateFormat().format(date));
    }

    public Date getDate() {
        try {
            return getDefaultDateFormat().parse(getText());
        } catch (ParseException e) {
            return new Date();
        }
    }

    private class DatePanel extends JPanel implements ChangeListener {
        int startYear = 1970;
        int lastYear = 2100;

        JSpinner yearSpin;
        JSpinner monthSpin;
        JButton[][] daysButton = new JButton[6][7];

        DatePanel() {
            setLayout(new BorderLayout());
            JPanel topYearAndMonth = createYearAndMonthPanal();
            add(topYearAndMonth, BorderLayout.NORTH);
            JPanel centerWeekAndDay = createWeekAndDayPanal();
            add(centerWeekAndDay, BorderLayout.CENTER);

            reflushWeekAndDay();
        }

        private JPanel createYearAndMonthPanal() {
            Calendar cal = getCalendar();
            int currentYear = cal.get(Calendar.YEAR);
            int currentMonth = cal.get(Calendar.MONTH) + 1;

            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());

            yearSpin = new JSpinner(new SpinnerNumberModel(currentYear,
                                                           startYear, lastYear, 1));
            yearSpin.setPreferredSize(new Dimension(70, 25));
            yearSpin.setSize(new Dimension(70, 25));
            yearSpin.setName("Year");
            yearSpin.setEditor(new JSpinner.NumberEditor(yearSpin, "####"));
            yearSpin.addChangeListener(this);
            panel.add(yearSpin);

            JLabel yearLabel = new JLabel("Year");
            panel.add(yearLabel);

            monthSpin = new JSpinner(new SpinnerNumberModel(currentMonth, 1,
                                                            12, 1));
            monthSpin.setSize(new Dimension(55, 25));
            monthSpin.setPreferredSize(new Dimension(55, 25));
            monthSpin.setName("Month");
            monthSpin.addChangeListener(this);
            panel.add(monthSpin);

            JLabel monthLabel = new JLabel("Month");
            panel.add(monthLabel);

            return panel;
        }

        private JPanel createWeekAndDayPanal() {
            String[] dayLabels = {"M", "T", "W", "T", "F", "Sat", "Sun"};
            JPanel panel = new JPanel();
            panel.setFont(new Font("Arial", Font.PLAIN, 10));
            panel.setLayout(new GridLayout(7, 7));

            for (int i = 0; i < 7; i++) {
                JLabel cell = new JLabel(dayLabels[i]);
                cell.setHorizontalAlignment(JLabel.CENTER);
                if (i == 5 || i == 6) {
                    cell.setForeground(weekendFontColor);
                }
                panel.add(cell);
            }

            int actionCommandId = 0;
            for (int i = 0; i < 6; i++)
                for (int j = 0; j < 7; j++) {
                    JButton numBtn = new JButton();
                    numBtn.setBorder(null);
                    numBtn.setHorizontalAlignment(SwingConstants.CENTER);
                    numBtn.setActionCommand(String.valueOf(actionCommandId));
                    numBtn.addActionListener(event -> {
                        JButton source = (JButton) event.getSource();
                        if (source.getText().length() == 0) {
                            return;
                        }
                        dayColorUpdate(true);
                        int newDay = Integer.parseInt(source.getText());
                        Calendar cal = getCalendar();
                        cal.set(Calendar.DAY_OF_MONTH, newDay);
                        setDate(cal.getTime());

                        dateDialog.setVisible(false);
                    });

                    if (j == 5 || j == 6)
                        numBtn.setForeground(weekendFontColor);

                    daysButton[i][j] = numBtn;
                    panel.add(numBtn);
                    actionCommandId++;
                }

            return panel;
        }

        private Calendar getCalendar() {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(getDate());
            return calendar;
        }

        private int getSelectedYear() {
            return (Integer) yearSpin.getValue();
        }

        private int getSelectedMonth() {
            return (Integer) monthSpin.getValue();
        }

        private void dayColorUpdate(boolean isOldDay) {
            Calendar cal = getCalendar();
            int day = cal.get(Calendar.DAY_OF_MONTH);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            int actionCommandId = day - 2 + cal.get(Calendar.DAY_OF_WEEK);
            int i = actionCommandId / 7;
            int j = actionCommandId % 7;
        }

        private void reflushWeekAndDay() {
            Calendar cal = getCalendar();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            int maxDayNo = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            int dayNo = 3 - cal.get(Calendar.DAY_OF_WEEK);
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 7; j++) {
                    String s = "";
                    if (dayNo >= 1 && dayNo <= maxDayNo) {
                        s = String.valueOf(dayNo);
                    }
                    if (defaultBackground != null) {
                        daysButton[i][j].setBackground(defaultBackground);
                    }
                    if (getSelectedYear() == currentYear) {
                        if (getSelectedMonth() == currentMonth) {
                            if (dayNo == currentday) {
                                defaultBackground = daysButton[i][j].getBackground();
                                daysButton[i][j].setBackground(currentDayBackgroundColor);
                            }
                        }
                    }
                    daysButton[i][j].setText(s);
                    dayNo++;
                }
            }
            dayColorUpdate(false);
        }

        public void stateChanged(ChangeEvent e) {
            dayColorUpdate(true);

            JSpinner source = (JSpinner) e.getSource();
            Calendar cal = getCalendar();
            if (source.getName().equals("Year")) {
                cal.set(Calendar.YEAR, getSelectedYear());
            } else {
                cal.set(Calendar.MONTH, getSelectedMonth() - 1);
            }
            setDate(cal.getTime());
            reflushWeekAndDay();
        }
    }
}