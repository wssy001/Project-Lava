package moe.ofs.backend.domain.admin.message;

import moe.ofs.backend.domain.dcs.BaseEntity;

public class Message extends BaseEntity {
    private int index;
    private int duration;
    private String content;
    private String prefix;
    private String suffix;
    private int interval;

    private Message() {}

    public Message(String content) {
        this.content = content;
        this.duration = 5;
    }

    public Message(String content, int duration) {
        this(content);
        this.duration = duration;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getDuration() {
        return duration;
    }

    public String getContent() {
        return content;
    }

    public int getWaitNextMessage() {
        return interval + duration;
    }

    public String toString() {
        return content;
    }
}
