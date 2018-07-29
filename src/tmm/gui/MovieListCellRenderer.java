package tmm.gui;

import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NORTHWEST;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingWorker;

import tmm.Movie;
import tmm.utils.Cache;
import tmm.utils.ImageUtils;

public class MovieListCellRenderer implements ListCellRenderer<Movie> {

    public static final int THUMB_HEIGHT = 200; // pixels

    private Cache<Movie, Icon> thumbCache = new Cache<>(1000, 10, TimeUnit.MINUTES);

    private Icon loading;

    public MovieListCellRenderer(Icon loading) {
        this.loading = loading;
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Movie> list, Movie movie, int index, boolean isSelected, boolean cellHasFocus) {
        Renderer cellRenderer = new Renderer();
        cellRenderer.labelMovieName.setText(createMovieLabel(movie));

        Icon thumb = thumbCache.get(movie);
        if (thumb == null) {
            cellRenderer.labelThumbnail.setIcon(loading);
            new ImageLoader(movie, list).execute();
        } else {
            cellRenderer.labelThumbnail.setIcon(thumb);
            cellRenderer.labelThumbnail.setPreferredSize(new Dimension(thumb.getIconWidth(), thumb.getIconHeight()));
        }

        if (isSelected) {
            cellRenderer.setBackground(list.getSelectionBackground());
            cellRenderer.setForeground(list.getSelectionForeground());
        } else {
            cellRenderer.setBackground(list.getBackground());
            cellRenderer.setForeground(list.getForeground());
        }

        // cellRenderer.setPreferredSize(new Dimension(400, cellRenderer.getPreferredSize().height));
        return cellRenderer;
    }

    private String createMovieLabel(Movie movie) {
        StringBuilder sb = new StringBuilder("<html><body style=\"font-family: sans-serif; font-size: 10pt\"><b>");
        sb.append(movie.title).append("</b>");
        sb.append("<br/><br/>");
        sb.append(movie.widthInPixel + "x" + movie.heightInPixel);
        sb.append("<br/>");
        for (String audio : movie.audioStreams) {
            sb.append(audio).append("<br/>");
        }
        if (!movie.tags.isEmpty()) {
            sb.append("<br/>");
            for (String tag : movie.tags) {
                sb.append(tag).append("<br/>");
            }
        }
        sb.append("<br/>");
        sb.append(movie.description);
        sb.append("</body></html>");
        return sb.toString();
    }

    private class Renderer extends JPanel {
        private static final long serialVersionUID = 1L;
        JLabel labelThumbnail = new JLabel();
        JEditorPane labelMovieName = new JEditorPane();

        private Renderer() {
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = NORTHWEST;
            labelThumbnail.setPreferredSize(new Dimension((int) (THUMB_HEIGHT / 1.33d), THUMB_HEIGHT));
            labelThumbnail.setBorder(BorderFactory.createLineBorder(Color.black));
            add(labelThumbnail, gbc);

            gbc.gridx = 1;
            gbc.fill = HORIZONTAL;
            gbc.weightx = 1;
            labelMovieName.setContentType("text/html");
            labelMovieName.setOpaque(false);
            labelMovieName.setPreferredSize(new Dimension(200, THUMB_HEIGHT));
            add(labelMovieName, gbc);
        }
    }

    public class ImageLoader extends SwingWorker<Icon, Void> {
        Movie movie;

        Icon smallThumbIcon;
        JList<? extends Movie> list;

        public ImageLoader(Movie movie, JList<? extends Movie> list) {
            this.movie = movie;
            this.list = list;
        }

        @Override
        protected Icon doInBackground() throws Exception {
            File thumbnail = new File(movie.pathToPoster);
            Image thumbnailImage = ImageUtils.loadThumbnail(thumbnail, THUMB_HEIGHT);
            smallThumbIcon = new ImageIcon(thumbnailImage);
            return smallThumbIcon;
        }

        @Override
        protected void done() {
            if (smallThumbIcon != null) {
                thumbCache.put(movie, smallThumbIcon);
                list.repaint();
            }
        }
    }
}
