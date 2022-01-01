/*
 * Copyright (c) Henrik Niehaus
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the project (Lazy Bones) nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package tmm;

import devplugin.*;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tmm.gui.HitDialog;
import tmm.gui.TMMSettingsPanel;
import util.ui.UiUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Integrates the TMM database into TV-Browser
 *
 * @author <a href="hampelratte@users.sf.net">hampelratte@users.sf.net</a>
 */
public class TMM extends Plugin {

    private static final Logger logger = LoggerFactory.getLogger(TMM.class);

    /**
     * Translator
     */
    private static final util.i18n.Localizer mLocalizer = util.i18n.Localizer.getLocalizerFor(TMM.class);

    private Properties settings;

    private boolean initialized = false;
    private List<Movie> movies;
    private int searchThreshold = 50;

    @Override
    public void handleTvBrowserStartFinished() {
        super.handleTvBrowserStartFinished();
        initialize();
    }

    private void initialize() {
        try {
            loadTmmDatabase();
            initialized = true;
        } catch (IllegalStateException ise) {
            logger.error("Error while loading the tinyMediaManager movie database", ise);
            JOptionPane.showMessageDialog(null,
                    getTranslation("database_locked", "Couldn't open database. Maybe it is in use by tinyMediaManager?!?"), getTranslation("error", "Error"),
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            logger.error("Error while loading the tinyMediaManager movie database", e);
        }
    }

    private void loadTmmDatabase() throws FileNotFoundException {
        File movieDatabaseFile = getMovieDatabaseFile();
        List<String> movieMarkups = loadMoviesFromDatabase(movieDatabaseFile);
        movies = parseMovies(movieMarkups);
        logger.info("tineMediaManager database contains {} movies", movies.size());
    }

    private List<Movie> parseMovies(List<String> movieMarkups) {
        List<Movie> parsedMovies = new ArrayList<>();
        for (String markup : movieMarkups) {
            JSONObject json = new JSONObject(markup);
            try {
                Movie movie = MovieParser.parse(json);
                parsedMovies.add(movie);
            } catch (Exception e) {
                logger.error("Couldn't parse movie:\n" + json, e);
            }
        }
        return parsedMovies;
    }

    private List<String> loadMoviesFromDatabase(File movieDatabaseFile) {
        MVStore s = MVStore.open(movieDatabaseFile.getAbsolutePath());
        MVMap<Integer, String> map = s.openMap("movies");
        List<String> markups = new ArrayList<>();
        for (Entry<Integer, String> entry : map.entrySet()) {
            String value = entry.getValue();
            markups.add(value);
        }
        s.close();
        return markups;
    }

    private File getMovieDatabaseFile() throws FileNotFoundException {
        logger.info("Loading tinyMediaManager database");
        String directory = settings.getProperty("tmm_data_dir");
        File movieDatabaseFile = new File(directory, "movies.db");
        if (!movieDatabaseFile.exists()) {
            throw new FileNotFoundException("Movie database does not exist at " + movieDatabaseFile);
        }
        return movieDatabaseFile;
    }

    @Override
    public void loadSettings(Properties settings) {
        this.settings = settings;
        setDefaults(settings);
        searchThreshold = Integer.parseInt(settings.getProperty("search_threshold"));
    }

    private void setDefaults(Properties settings) {
        setIfNotExist(settings, "tmm_data_dir", "/opt/tmm/data");
        setIfNotExist(settings, "search_threshold", Integer.toString(searchThreshold));
    }

    private void setIfNotExist(Properties settings, String key, String value) {
        if (!settings.containsKey(key)) {
            settings.setProperty(key, value);
        }
    }

    @Override
    public Properties storeSettings() {
        return settings;
    }

    @Override
    public ActionMenu getContextMenuActions(final Program program) {
        AbstractAction searchAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (!initialized) {
                    initialize();
                    if (!initialized) {
                        logger.warn("tinyMediaManager plugin is not initialized");
                        JOptionPane.showMessageDialog(null,
                                getTranslation("not_initialized", "An error occured during initialization."));
                        return;
                    }
                }

                String title = program.getTitle();
                logger.info("Searching [{}] in TMM DB", title);
                List<Movie> hits = new ArrayList<>();
                for (Movie movie : movies) {
                    int percentageOfEquality = StringUtils.percentageOfEquality(title, movie.title);
                    if (percentageOfEquality > searchThreshold) {
                        logger.info("Found [{}] with {}%", movie.title, percentageOfEquality);
                        hits.add(movie);
                        movie.hitPercentage = percentageOfEquality;
                    }
                }
                if (hits.isEmpty()) {
                    JOptionPane.showMessageDialog(null, getTranslation("not_found", "Nothing similar found"));
                } else {
                    HitDialog hitDialog = new HitDialog(TMM.this, hits);
                    hitDialog.setTitle(getTranslation("tmm", "tinyMediaManager"));
                    hitDialog.setSize(800, 600);
                    hitDialog.setIconImage(createImageIcon("tmm/tmm16.png").getImage());
                    UiUtilities.centerAndShow(hitDialog);
                }
            }
        };
        searchAction.putValue(Action.NAME, TMM.getTranslation("search_in_db", "Search in tinyMediaManager"));
        searchAction.putValue(Action.SMALL_ICON, createImageIcon("tmm/tmm16.png"));
        return new ActionMenu(searchAction);
    }

    public Icon getIconFromJar(String name) {
        return createImageIcon(name);
    }

    @Override
    public SettingsTab getSettingsTab() {
        return new TMMSettingsPanel(settings);
    }

    @Override
    public PluginInfo getInfo() {
        String name = TMM.getTranslation("tmm", "TMM");
        String description = TMM.getTranslation("desc", "This plugin integrates the tinyMediaManager database into TV-Browser.");
        String author = "Henrik Niehaus, henrik.niehaus@gmx.de";
        return new PluginInfo(getClass(), name, description, author, "BSD", "http://hampelratte.org/blog");
    }

    public static Version getVersion() {
        // return new Version(0, 0, false, "snapshot-01-05-2013");
        return new Version(1, 00, 0, true);
    }

    @Override
    public String getMarkIconName() {
        return "tmm/tmm16.png";
    }


    public static String getTranslation(String key, String altText) {
        return mLocalizer.msg(key, altText);
    }
}
