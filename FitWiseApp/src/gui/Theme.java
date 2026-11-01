package gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.net.URL;

public final class Theme {
    private Theme() {}

    public static final Color BG = new Color(4, 6, 8);
    public static final Color CARD_BG = new Color(10, 14, 16, 238);
    public static final Color CARD_SOFT = new Color(17, 22, 24, 242);
    public static final Color CARD_LIGHT = new Color(27, 33, 35);
    public static final Color ACCENT = new Color(190, 255, 0);
    public static final Color ACCENT_SOFT = new Color(190, 255, 0, 55);
    public static final Color ACCENT_DARK = new Color(65, 88, 0);
    public static final Color TEXT = new Color(248, 250, 248);
    public static final Color MUTED_TEXT = new Color(157, 168, 164);
    public static final Color GOOD = new Color(190, 255, 0);
    public static final Color WARN = new Color(255, 193, 76);
    public static final Color RED = new Color(255, 83, 95);
    public static final Color CYAN = new Color(70, 220, 220);

    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 32);
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONT_HEADER = new Font("SansSerif", Font.BOLD, 18);
    public static final Font FONT_LABEL = new Font("SansSerif", Font.BOLD, 12);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_BIG = new Font("SansSerif", Font.BOLD, 28);
    public static final Font FONT_HERO = new Font("SansSerif", Font.BOLD, 48);

    /**
     * Full application background. It deliberately paints the supplied
     * fitness UI image on the right so it remains visible on every screen.
     */
    public static class BackgroundPanel extends JPanel {
        private final Image base;

        public BackgroundPanel() {
            this("assets/fitness_bg.png");
        }

        public BackgroundPanel(String path) {
            setOpaque(false);
            base = loadAsset(path);
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth(), h = getHeight();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            g2.setColor(BG);
            g2.fillRect(0, 0, w, h);

            // Base atmospheric fitness background.
            drawCover(g2, base, 0, 0, w, h, 0.45f);

            // Strong dark veil keeps content readable.
            g2.setColor(new Color(0, 0, 0, 215));
            g2.fillRect(0, 0, w, h);

            // Keep feature pages clean: the reference screenshot is not painted
            // over the live UI. The real controls and personalized data stay visible.
            // Lime cinematic glow.
            GradientPaint glow = new GradientPaint(
                    0, h, new Color(190, 255, 0, 62),
                    (float) (w * .70), (float) (h * .20), new Color(0, 0, 0, 0)
            );
            g2.setPaint(glow);
            g2.fillRect(0, 0, w, h);

            // Subtle top-to-bottom vignette.
            GradientPaint vignette = new GradientPaint(
                    0, 0, new Color(0, 0, 0, 105),
                    0, h, new Color(0, 0, 0, 25)
            );
            g2.setPaint(vignette);
            g2.fillRect(0, 0, w, h);
            g2.dispose();
        }

        private void drawCover(Graphics2D g2, Image img, int x, int y, int w, int h, float alpha) {
            if (img == null || img.getWidth(null) <= 0 || img.getHeight(null) <= 0) return;
            double ir = (double) img.getWidth(null) / img.getHeight(null);
            double pr = (double) w / Math.max(1, h);
            int dw, dh;
            if (pr > ir) { dw = w; dh = (int) (w / ir); }
            else { dh = h; dw = (int) (h * ir); }
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.drawImage(img, dx, dy, dw, dh, this);
            g2.setComposite(old);
        }
    }

    public static class RoundedPanel extends JPanel {
        private final int radius;
        public RoundedPanel(LayoutManager l, int r) { super(l); radius = r; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(CARD_BG);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth()-1, getHeight()-1, radius, radius));
            g2.setColor(new Color(255,255,255,32));
            g2.draw(new RoundRectangle2D.Double(.5,.5,getWidth()-2,getHeight()-2,radius,radius));
            g2.setColor(ACCENT_SOFT);
            g2.draw(new RoundRectangle2D.Double(1.5,1.5,getWidth()-4,getHeight()-4,Math.max(4,radius-2),Math.max(4,radius-2)));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JPanel cardPanel(LayoutManager l) {
        RoundedPanel p = new RoundedPanel(l, 22);
        p.setBorder(new EmptyBorder(18,18,18,18));
        return p;
    }
    public static JPanel darkPanel(LayoutManager l) {
        RoundedPanel p = new RoundedPanel(l, 17);
        p.setBorder(new EmptyBorder(10,12,10,12));
        return p;
    }
    public static JPanel transparentPanel(LayoutManager l) { JPanel p = new JPanel(l); p.setOpaque(false); return p; }

    public static JScrollPane transparentScrollPane(Component c) {
        JScrollPane s = new JScrollPane(c);
        s.setOpaque(false); s.getViewport().setOpaque(false); s.setBorder(null);
        s.getVerticalScrollBar().setUnitIncrement(16);
        return s;
    }

    public static JLabel title(String s) { JLabel l=new JLabel(s);l.setFont(FONT_TITLE);l.setForeground(TEXT);return l; }
    public static JLabel header(String s) { JLabel l=new JLabel(s);l.setFont(FONT_HEADER);l.setForeground(ACCENT);return l; }
    public static JLabel subtitle(String s) { JLabel l=new JLabel(s);l.setFont(FONT_SUBTITLE);l.setForeground(MUTED_TEXT);return l; }
    public static JLabel bodyLabel(String s) { JLabel l=new JLabel(s);l.setFont(FONT_BODY);l.setForeground(TEXT);return l; }
    public static JLabel mutedLabel(String s) { JLabel l=new JLabel(s);l.setFont(FONT_BODY);l.setForeground(MUTED_TEXT);return l; }

    public static class RoundedButton extends JButton {
        private final Color fill;
        public RoundedButton(String text, Color fill, Color fg) {
            super(text); this.fill=fill; setForeground(fg); setFont(FONT_LABEL); setFocusPainted(false);
            setBorder(new EmptyBorder(12,18,12,18)); setContentAreaFilled(false); setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            Color c=getModel().isPressed()?fill.darker():(getModel().isRollover()?fill.brighter():fill);
            g2.setColor(c);g2.fillRoundRect(0,0,getWidth(),getHeight(),18,18);g2.dispose();super.paintComponent(g);
        }
    }
    public static JButton primaryButton(String s){return new RoundedButton(s,ACCENT,Color.BLACK);}
    public static JButton secondaryButton(String s){return new RoundedButton(s,new Color(28,34,36),TEXT);}

    public static void styleTextField(JTextComponent f){
        f.setBackground(new Color(18,23,25)); f.setForeground(TEXT); f.setCaretColor(ACCENT);
        f.setFont(new Font("SansSerif", Font.PLAIN, 16));
        f.setBorder(new CompoundBorder(new LineBorder(new Color(255,255,255,70),1),new EmptyBorder(7,12,7,12)));
        f.setMinimumSize(new Dimension(100,44));
        f.setPreferredSize(new Dimension(300,48));
    }
    public static void styleComboBox(JComboBox<?> b){
        b.setBackground(new Color(18,23,25));b.setForeground(TEXT);b.setFont(new Font("SansSerif", Font.PLAIN, 16));
        b.setBorder(new LineBorder(new Color(255,255,255,70),1));
        b.setMinimumSize(new Dimension(100,44));
        b.setPreferredSize(new Dimension(300,48));
        b.setRenderer(new DefaultListCellRenderer(){
            @Override public Component getListCellRendererComponent(JList<?> list,Object value,int index,boolean selected,boolean focus){
                JLabel label=(JLabel)super.getListCellRendererComponent(list,value,index,selected,focus);
                label.setFont(new Font("SansSerif",Font.PLAIN,16)); label.setBorder(new EmptyBorder(5,8,5,8));
                if(!selected){label.setBackground(new Color(18,23,25));label.setForeground(TEXT);}
                return label;
            }
        });
    }
    public static Color colorForBmiCategory(String c){if("Normal".equals(c))return GOOD;if("Obese".equals(c))return RED;return WARN;}

    /** A real painted image panel, not a JLabel placeholder. */
    public static class FitnessImagePanel extends JPanel {
        private final Image image;
        public FitnessImagePanel(String path) { setOpaque(false); image=loadAsset(path); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(7,10,12,245));g2.fillRoundRect(0,0,getWidth(),getHeight(),26,26);
            if(image!=null){
                double ir=(double)image.getWidth(null)/Math.max(1,image.getHeight(null));
                double pr=(double)getWidth()/Math.max(1,getHeight());
                int dw,dh;if(pr>ir){dw=getWidth();dh=(int)(getWidth()/ir);}else{dh=getHeight();dw=(int)(getHeight()*ir);}
                int x=(getWidth()-dw)/2,y=(getHeight()-dh)/2;
                g2.drawImage(image,x,y,dw,dh,this);
            }
            g2.setColor(new Color(0,0,0,25));g2.fillRoundRect(0,0,getWidth(),getHeight(),26,26);
            g2.setColor(new Color(190,255,0,120));g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,26,26);
            g2.dispose();
        }
    }

    public static JLabel fitnessVisual(int width,int height){
        JLabel label=new JLabel();
        label.setPreferredSize(new Dimension(width,height));
        Image image=loadAsset("assets/fitwise_reference.png");
        if(image!=null){
            label.setIcon(new ImageIcon(image.getScaledInstance(width,height,Image.SCALE_SMOOTH)));
        }
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        return label;
    }

    public static Image loadAsset(String path){
        // 1) current working directory
        File f=new File(path); if(f.exists()) return new ImageIcon(f.getAbsolutePath()).getImage();
        // 2) executable/project directory
        try {
            File here=new File(System.getProperty("user.dir"));
            File[] candidates={new File(here,path),new File(here,"FitWiseApp"+File.separator+path)};
            for(File c:candidates) if(c.exists()) return new ImageIcon(c.getAbsolutePath()).getImage();
        } catch(Exception ignored) {}
        // 3) classpath
        String clean=path.startsWith("/")?path.substring(1):path;
        URL u=Theme.class.getClassLoader().getResource(clean);
        return u==null?null:new ImageIcon(u).getImage();
    }
}
