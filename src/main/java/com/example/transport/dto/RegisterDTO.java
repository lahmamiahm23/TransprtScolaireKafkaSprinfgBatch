package com.example.transport.dto;

/**
 * DTO pour l'inscription d'un utilisateur (Parent ou Driver)
 */
public class RegisterDTO {

    private String firstName;
    private String lastName;
    private String username; // nouveau champ pour le login unique
    private String email;
    private String phone;
    private String password;
    private String role; // "PARENT" ou "DRIVER"


    // =================== Constructeurs ===================

    /**
     * Constructeur par défaut requis par Jackson
     */
    public RegisterDTO() {}

    /**
     * Constructeur complet
     */
    public RegisterDTO(String firstName, String lastName, String username, String email,
                       String phone, String password, String role, String licenseNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.role = role;

    }

    // =================== Getters / Setters ===================

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }


}
