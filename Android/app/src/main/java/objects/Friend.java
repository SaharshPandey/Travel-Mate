package objects;

public class Friend {
    private int _id;
    private String name;
    private String contact;

    public Friend(int _id, String name,String contact){
        this.name = name;
        this.contact = contact;
        this._id = _id;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}
