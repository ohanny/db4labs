package fr.icodem.db4labs.event;

public class NavigationEvent {
    private String page;
    private boolean back;
    private String title;//TODO ajouter title

    public NavigationEvent(boolean back) {
        this.back = back;
    }

    public NavigationEvent(String page, String title) {
        this.page = page;
        this.title = title;
    }

    public String getPage() {
        return page;
    }

    public boolean isBack() {
        return back;
    }

    public void setBack(boolean back) {
        this.back = back;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
}
