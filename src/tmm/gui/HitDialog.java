package tmm.gui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;

import tmm.Movie;
import tmm.TMM;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

public class HitDialog extends JDialog implements WindowClosingIf {

    private DefaultListModel<Movie> listModel = new DefaultListModel<>();
    private JList<Movie> list = new JList<>(listModel);

    public HitDialog(TMM plugin, List<Movie> hits) {
        Collections.sort(hits, new Comparator<Movie>() {
            @Override
            public int compare(Movie o1, Movie o2) {
                return ((Integer) o2.hitPercentage).compareTo(o1.hitPercentage);
            }
        });

        Icon loading = plugin.getIconFromJar("tmm/loading.png");
        list.setCellRenderer(new MovieListCellRenderer(loading));
        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        list.setVisibleRowCount(-1);
        listModel.clear();
        for (Movie movie : hits) {
            listModel.addElement(movie);
        }
        getContentPane().add(new JScrollPane(list));

        UiUtilities.registerForClosing(this);
    }

    @Override
    public void close() {
        dispose();
    }

}
