package com.morph.deadline;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.morph.deadline.component.DateTextField;
import com.sun.javafx.css.converters.FontConverter;
import dorkbox.notify.Notify;
import dorkbox.notify.Pos;
import dorkbox.notify.Theme;
import net.miginfocom.layout.CC;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

public class Deadline extends JFrame implements ActionListener {
    private final static SimpleDateFormat sf = new SimpleDateFormat("dd:HH:mm:ss");
    private final static Font HELVETICA_BIG_BOLD = new Font("Serif", Font.BOLD, 100);

    private JPanel mainPanel;
    private JLabel deadlineCounterLabel;
    private Date deadLine = null;
    private Timer timer = new Timer(100, this);
    private JMenuBar menuBar;
    private Preferences preferences;
    private boolean notified = false;
    List<Image> images;

    private Deadline() {
        initPreferences();
        initLookAndFeel();
        initMenu();
        initFields();
        initUI();
    }

    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    private void initFields() {
        MigLayout migLayout = new MigLayout();
        mainPanel = new JPanel(migLayout);
    }

    private void initMenu() {
        menuBar = new JMenuBar();
        JMenu deadline = new JMenu("Deadline");
        JMenuItem setNewDeadline = new JMenuItem("Set new deadline");
        setNewDeadline.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JPanel panel = new JPanel();
                Date newDeadlineDate = new Date();
                DateTextField dateTextField = new DateTextField("yyyy-MM-dd", newDeadlineDate);
                JDialog jDialog = new JDialog(Deadline.this);
                jDialog.setTitle("Set new deadline");
                jDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                panel.add(dateTextField);
                JButton apply = new JButton("apply");
                apply.addActionListener(e1 -> {
                    if (timer.isRunning()) {
                        timer.stop();
                    }
                    deadLine = dateTextField.getDate();
                    preferences.put("deadline", String.valueOf(dateTextField.getDate().getTime()));
                    initDeadlineCounterLabel();
                    Deadline.this.pack();
                    timer.start();
                    jDialog.setVisible(false);
                });
                panel.add(apply);
                jDialog.add(panel);
                jDialog.setSize(200, 70);
                jDialog.setResizable(false);
                setCenterLocation(jDialog);
                jDialog.show();

            }
        });
        deadline.add(setNewDeadline);
        menuBar.add(deadline);
        this.setJMenuBar(menuBar);
    }

    private void setCenterLocation(JDialog child) {
        Window owner = child.getOwner();
        int ownerX = owner.getLocation().x;
        int ownerY = owner.getLocation().y;
        int ownerWidth = owner.getWidth();
        int ownerHeight = owner.getHeight();
        int childWidth = child.getWidth();
        int childHeight = child.getHeight();

        ownerX += ((ownerWidth / 2) - (childWidth / 2));
        ownerY += ((ownerHeight / 2) - (childHeight / 2));

        child.setLocation(ownerX, ownerY);
    }

    private void initUI() {

        this.add(mainPanel);
        this.setIconImages(loadIcons());
        this.setResizable(false);

        setTitle("Deadline reminder");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initPanelComponents();
    }

    private void initPreferences() {
        preferences = Preferences.userNodeForPackage(Deadline.class);
        String deadline = preferences.get("deadline", null);
        if (!StringUtils.isEmpty(deadline)){
            deadLine = new Date(Long.parseLong(deadline));
            timer.start();
        }
    }

    private Image loadIcon(String iconPath) {
        URL imgURL = ClassLoader.getSystemResource(iconPath);
        if (imgURL != null) {
            return new ImageIcon(imgURL).getImage();
        } else {
            System.out.println("load failed");
            return null;
        }
    }

    private List<Image> loadIcons() {
        images = new ArrayList<>();
        add(images, loadIcon("icons/16.png"));
        add(images, loadIcon("icons/24.png"));
        add(images, loadIcon("icons/32.png"));
        add(images, loadIcon("icons/48.png"));
        add(images, loadIcon("icons/64.png"));
        add(images, loadIcon("icons/72.png"));
        add(images, loadIcon("icons/96.png"));
        add(images, loadIcon("icons/128.png"));
        add(images, loadIcon("icons/144.png"));
        add(images, loadIcon("icons/152.png"));
        add(images, loadIcon("icons/192.png"));
        add(images, loadIcon("icons/256.png"));
        add(images, loadIcon("icons/512.png"));
        add(images, loadIcon("icons/1024.png"));
        add(images, loadIcon("icons/2048.png"));
        return images;
    }

    private void add(List<Image> list, Image image) {
        if (list == null || image == null) {
            return;
        }
        list.add(image);
    }

    private void initPanelComponents() {
        JPanel labelPanel = new JPanel();
        JPanel counterPanel = new JPanel();
        labelPanel.add(createDeadlineLabel("DEADLINE"));
        mainPanel.add(labelPanel, new CC().dockNorth().alignX("center").spanX().growX().pushX().wrap());
        initDeadlineCounterLabel();
        counterPanel.add(deadlineCounterLabel);
        mainPanel.add(counterPanel, new CC().dockNorth().alignX("center").spanX().growX().pushX());
        this.pack();
        repaint();
    }

    private void initDeadlineCounterLabel() {
        Date dateNow = new Date();
        if (deadLine == null) {
            deadLine = new Date();
        }
        long remaining = deadLine.getTime() - dateNow.getTime();
        if (deadlineCounterLabel == null) {
            deadlineCounterLabel = createDeadlineLabel(formatCounter(remaining));
        } else {
            deadlineCounterLabel.setText(formatCounter(remaining));
        }
        repaint();
    }

    private JLabel createDeadlineLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.RED);
        label.setFont(HELVETICA_BIG_BOLD);
        return label;
    }

    public static void main(String[] args) {

        EventQueue.invokeLater(() -> {
            JFrame ex = new Deadline();
            ex.setVisible(true);
        });
    }

    private String formatCounter(long remainingTimeInMs) {
        long seconds = remainingTimeInMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        seconds = seconds - (minutes * 60);
        minutes = minutes - (hours * 60);
        hours = hours - (days * 24);
        String format = String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
        if (minutes == 0 && seconds == 0 && !notified) {
            notify(format);
            notified = true;
        }
        if (minutes != 0 && seconds != 0 && notified){
            notified = false;
        }
        return format;
    }

    private void notify(String text) {
        Notify.create()
              .title("Deadline reminder")
              .text("You have left " + text)
              .text(new Theme(helveticaBold(20), helveticaBold(18), Color.DARK_GRAY, Color.GRAY, Color.LIGHT_GRAY, Color.GRAY, Color.gray))
              .image(images.get(14))
              .darkStyle()
              .show();
    }

    private String helveticaBold(int fontSize) {
        String result = "Serif bold ";
        if (fontSize <= 0) {
            result += "14";
        } else {
            result += fontSize;
        }
        return result;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        initDeadlineCounterLabel();
    }
}
