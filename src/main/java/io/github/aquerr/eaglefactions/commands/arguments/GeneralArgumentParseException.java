package io.github.aquerr.eaglefactions.commands.arguments;

import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.text.Text;

public class GeneralArgumentParseException extends ArgumentParseException {

    private Text text;

    public GeneralArgumentParseException(Text message) {
        super(message, null, 0);
        this.text = message;
    }

    @Override
    public Text getText() {
        return text;
    }

    @Override
    public boolean shouldIncludeUsage() {
        return false;
    }
}
