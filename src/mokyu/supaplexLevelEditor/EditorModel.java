/*
 * Copyright (C) 2019 Mokyu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mokyu.supaplexLevelEditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;
import java.beans.*;
import mokyu.libsupaplex.*;

/**
 * Don't forget to call init() after construction
 *
 * @author Mokyu
 */
public class EditorModel implements PropertyChangeListener {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    // listener 
    public interface Listener {

        public void modelChanged();
    }
    private final List<Listener> listeners = new ArrayList<>();

    public void addListener(EditorModel.Listener listener) {
        listeners.remove(listener);
        listeners.add(listener);
    }

    public void removeListener(EditorModel.Listener listener) {
        listeners.remove(listener);
    }

    public void fireChanged() {
        listeners.forEach((listener) -> {
            listener.modelChanged();
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        // pass property change to Controller
        pcs.firePropertyChange(e);
    }
    /**
     * Register controller to this.
     * @param e Change event
     */
    public void addPropertyChangeListener(PropertyChangeListener e) {
        pcs.addPropertyChangeListener(e);
    }

    // properties
    private Supaplex supaplex;
    private Properties properties;
    private boolean dataChanged;
    
    // UI State related stuff
    private Integer currentLevelSlot;

    /**
     * Change level slot, fired property change and notifies the controller.
     * @param currentLevelSlot 
     */
    public void setCurrentLevelSlot(Integer currentLevelSlot) {
        Integer old = getCurrentLevelSlot();
        this.currentLevelSlot = currentLevelSlot;
        pcs.firePropertyChange("currentLevelSlot", old, currentLevelSlot);
    }

    public Integer getCurrentLevelSlot() {
        return currentLevelSlot;
    }
    
    public EditorModel() {
        supaplex = new Supaplex();
        supaplex.init();
        properties = new Properties();
        dataChanged = false;
        currentLevelSlot = 1;
        File props = new File("config.properties");
        if (props.exists()) {
            InputStream fstream;
            try {
                fstream = new FileInputStream(props);
                properties.load(fstream);
                fstream.close();
                return;
            } catch (FileNotFoundException ex) {
                properties.setProperty("language", "EN");
                System.out.println(properties.getProperty("language"));
            } catch (IOException ex) {
                properties.setProperty("language", "EN");
            }
        } else {
            setProperty("language", "EN");
        }
    }

    /**
     * Register objects to the PCS post construction
     */
    public void init() {
        supaplex.addPropertyChangeListener(this);
    }

    /**
     * Set to true when there are changes to our data
     *
     * @return
     */
    public boolean getDataChanged() {
        return this.dataChanged;
    }

    public void setDataChanged(boolean value) {
        this.dataChanged = value;
    }

    /**
     * Overwrite existing level collection
     *
     * @param supaplex Supaplex object (for if you load a collection from a
     * file) or null to create an empty collection
     */
    public void setLevelCollection(Supaplex supaplex) {
        if (supaplex == null) {
            this.supaplex = new Supaplex();
        } else {
            this.supaplex = supaplex;
        }
        pcs.firePropertyChange("LevelCollection", 0, 1);
    }

    /**
     * Retrieve Supaplex object. Modifying the return result requires calling
     * setDataChanged(true) and fireChanged() manually
     *
     * @return The current Supaplex object.
     */
    public Supaplex getLevelCollection() {
        return this.supaplex;
    }

    ;

    /**
     * Get configuration property from config.properties
     *
     * @param key
     * @return String or null when key not found
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Set configuration property in config.properties
     *
     * @param key the key to set
     * @param value they value belonging to the key
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
        try {
            properties.store(new FileOutputStream("config.properties"), null);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EditorModel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EditorModel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        pcs.firePropertyChange("Properties", 0, 1);
    }

}
