package io.github.aquerr.eaglefactions.logic;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageReceiver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.spongepowered.api.text.format.TextColors.*;

public class FactionsPagination {

    private int page = 1;
    private int linesPerPage = 5;
    private String baseCommand = null;
    private Text title = Text.of(RED, "ERROR");
    private List<Text> contents = new ArrayList<>();

    private static final String padding = "__________________________________________";

    public FactionsPagination page(int pageNumber) {
        this.page = pageNumber;
        return this;
    }

    public FactionsPagination content(List<Text> contents) {
        this.contents = contents;
        return this;
    }

    public FactionsPagination append(Text... texts) {
        contents.addAll(Arrays.asList(texts));
        return this;
    }

    public FactionsPagination linesPerPage(int linesPerPage) {
        this.linesPerPage = linesPerPage;
        return this;
    }

    public FactionsPagination title(Text title){
        this.title = title;
        return this;
    }

    public FactionsPagination baseCommand(String baseCommand){
        this.baseCommand = baseCommand;
        return this;
    }

    //TODO: This may not work
    public void sendTo(MessageReceiver receiver) {
        Text pageChange = Text.of();
        if (baseCommand != null) {
            Text.Builder left = Text.builder("[<]").color(page > 1 ? AQUA : GRAY);
            Text.Builder right = Text.builder("[>]").color(page < (contents.size() / linesPerPage) + 1 ? AQUA : GRAY);
            if (page > 1) {
                left.onClick(TextActions.runCommand(baseCommand + " " + (page - 1)));
                left.onHover(TextActions.showText(Text.of(LIGHT_PURPLE, "Command: ", AQUA, baseCommand + " " + (page - 1))));
            }
            if (page < contents.size() / linesPerPage + 1) {
                right.onClick(TextActions.runCommand(baseCommand + " " + (page + 1)));
                right.onHover(TextActions.showText(Text.of(LIGHT_PURPLE, "Command: ", AQUA, baseCommand + " " + (page + 1))));
            }
            pageChange = Text.of(left, " " + page + "/" + (contents.size() / linesPerPage + 1) + " ", right, " ");
        }
        Text title = Text.of(GOLD, ".[ ", this.title, " ", pageChange, GOLD, "].");
        receiver.sendMessage(Text.of(GOLD, padding.substring(0, (50 - title.toPlain().length()) / 2), title, padding.substring(0, (50 - title.toPlain().length()) / 2)));
        if(page < 1 || page > contents.size() / linesPerPage + 1){
            receiver.sendMessage(Text.of(RED, "Invalid, page must be between 1 and " + (contents.size() / linesPerPage + 1) + '.'));
            return;
        }
        for (int i = (page - 1) * linesPerPage; i < page * linesPerPage && i < contents.size(); i++) {
            receiver.sendMessage(contents.get(i));
        }
    }
}
