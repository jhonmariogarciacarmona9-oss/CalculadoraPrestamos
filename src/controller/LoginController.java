package controller;

import db.ConexionDB;
import model.Usuario;
import view.CalculadoraView;
import view.LoginView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    private final LoginView loginView;

    public LoginController(LoginView loginView) {
        this.loginView = loginView;
        inicializarListeners();
    }

    private void inicializarListeners() {
        loginView.getBtnIngresar().addActionListener(this::intentarLogin);
        // Permitir Enter en el campo contraseña
        loginView.getTxtPassword().addActionListener(this::intentarLogin);
    }

    private void intentarLogin(ActionEvent e) {
        String username = loginView.getUsuario();
        String password = loginView.getPassword();

        if (username.isEmpty() || password.isEmpty()) {
            loginView.mostrarError("Por favor ingrese usuario y contraseña.");
            return;
        }

        String hashIngresado = Usuario.hashPassword(password);
        try (Connection conn = ConexionDB.getConexion()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM usuarios WHERE usuario = ? AND password = ?");
            ps.setString(1, username);
            ps.setString(2, hashIngresado);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                loginView.dispose();
                CalculadoraView calculadoraView = new CalculadoraView();
                calculadoraView.setVisible(true);
            } else {
                loginView.mostrarError("Usuario o contraseña incorrectos.");
            }
        } catch (SQLException ex) {
            loginView.mostrarError("Error de conexión a la base de datos.");
        }
    }
}

        try {
            Connection conn = ConexionDB.getInstance().getConexion();
            String sql = "SELECT id FROM usuarios WHERE username = ? AND password_hash = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, hashIngresado);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    loginView.dispose();
                    CalculadoraView calcView = new CalculadoraView();
                    new CalculadoraController(calcView);
                    calcView.setVisible(true);
                } else {
                    loginView.mostrarError("Usuario o contraseña incorrectos.");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(loginView,
                "Error de conexión con la base de datos:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
