package pk.gov.pbs.formbuilder.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import pk.gov.pbs.database.annotations.NotNull;
import pk.gov.pbs.database.annotations.Unique;

public class LoginPayload extends Table {
    @NotNull
    @Unique
    public String userName;

    @NotNull
    public String fullName;

    @NotNull
    public int gender;

    @NotNull
    public long expiry;

    @Nullable
    public String selectedBlock;

    @NotNull
    @Unique
    public String token;

    public LoginPayload(){
    }

    public LoginPayload(String userName, String fullName, int gender, long expiry, String selectedBlock, String token) {
        this.userName = userName;
        this.fullName = fullName;
        this.gender = gender;
        this.expiry = expiry;
        this.selectedBlock = selectedBlock;
        this.token = token;
    }

    public Long getId() {
        return aid;
    }

    public void setId(Long id) {
        this.aid = id;
    }

    @NonNull
    public String getUserName() {
        return userName;
    }

    public String getFullName(){ return fullName; }

    public long getExpiry() {
        return expiry;
    }

    public void expire() {
        this.expiry = 0;
    }

    @Nullable
    public String getToken() {
        return token;
    }

    public void setToken(@Nullable String token) {
        this.token = token;
    }

    @Nullable
    public String getSelectedBlock() {
        return selectedBlock;
    }

    public void setSelectedBlock(@Nullable String selectedBlock) {
        this.selectedBlock = selectedBlock;
    }
}
