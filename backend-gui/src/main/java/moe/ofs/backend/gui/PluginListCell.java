package moe.ofs.backend.gui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import moe.ofs.backend.Plugin;
import moe.ofs.backend.PluginClassLoader;
import moe.ofs.backend.util.I18n;

import java.io.IOException;

public class PluginListCell extends ListCell<Plugin> {
    Scene scene;

    VBox mainVBox = new VBox(5);
    HBox title = new HBox(10);
    HBox control = new HBox(5);
    Pane pane = new Pane();

    Label label = new Label();
    Label author = new Label();
    Label version = new Label();
    Label desc = new Label();

    Plugin plugin;

    Button controlButton = new Button();
    Button configButton = new Button("Config");

    private void switchPluginLoadState(Plugin plugin) {
        // get the loaded instance of the plugin
        if(plugin.isEnabled()) {
            plugin.disable();
            controlButton.setText("Enable");
        } else {
            plugin.enable();
            controlButton.setText("Disable");
        }
    }

    public PluginListCell(ListView<Plugin> listView) {
        super();

        title.getChildren().addAll(label, version, pane, author);
        HBox.setHgrow(pane, Priority.ALWAYS);
        mainVBox.getChildren().addAll(title, desc, control);
        control.getChildren().addAll(controlButton);
    }

    @Override
    protected void updateItem(Plugin item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);
        setGraphic(null);

        if (item != null && !empty) {
            plugin = Plugin.loadedPlugins.stream()
                    .filter(p -> p.equals(getItem()))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("Plugin Does Not Exist"));

            try {
                if(plugin.getPluginGui() != null)
                    control.getChildren().add(configButton);

            } catch (IOException e) {
                e.printStackTrace();
            }

            if(plugin.getLocalizedName() != null) {
                label.setText(plugin.getLocalizedName());
            } else {
                label.setText(plugin.getName());
            }
            I18n.localeProperty().addListener(observable -> {
                if(plugin.getLocalizedName() != null) {
                    label.setText(plugin.getLocalizedName());
                }
            });


            label.setStyle("-fx-font-weight: bold;");

            version.setText(plugin.getVersion());
            author.setText(plugin.getAuthor());
            controlButton.setText(plugin.isEnabled() ? "Disable" : "Enable");


            if(plugin.getLocalizedDescription() != null) {
                desc.setText(plugin.getLocalizedDescription());
            } else {
                desc.setText(plugin.getDescription());
            }
            I18n.localeProperty().addListener(observable -> {
                if(plugin.getLocalizedDescription() != null) {
                    desc.setText(plugin.getLocalizedDescription());
                }
            });

            controlButton.setOnAction(event -> switchPluginLoadState(plugin));
            configButton.setOnAction(event -> {
                try {
                    if(plugin.getPluginGui() != null) {
                        if(scene == null) {
                            Parent root = plugin.getPluginGui();
                            scene = new Scene(root);
                        }

                        JMetro jMetro = new JMetro(Style.LIGHT);
                        jMetro.setScene(scene);

                        Stage pluginStage = new Stage();
                        if(plugin.getLocalizedName() != null) {
                            pluginStage.setTitle(plugin.getLocalizedName());
                        } else {
                            pluginStage.setTitle(plugin.getName());
                        }

                        pluginStage.setScene(scene);
                        pluginStage.show();

                        I18n.localeProperty().addListener(observable -> {
                            if(plugin.getLocalizedName() != null)
                                pluginStage.setTitle(plugin.getLocalizedName());
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            setGraphic(mainVBox);
        }
    }
}
