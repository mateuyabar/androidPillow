package cat.my.android.pillow.data.users.guested;

import cat.my.android.pillow.IdentificableModel;

public interface IGuestedUser extends IdentificableModel{
	public void setGuest(boolean guest);
	public boolean isGuest();

    public String getEmail();
    public void setEmail(String email);

    public String getPassword();
    public void setPassword(String password);

    public String getAuthToken();
    public void setAuthToken(String authToken);
}