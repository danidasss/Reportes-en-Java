package registriestudiantes;

import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;

import java.nio.file.Paths;
import java.net.URISyntaxException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.net.URL;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.HashMap;

public class StudentApp extends JFrame {
    private JTextField txtNombre, txtApellido, txtCI, txtEdad, txtEstado, txtCiudad, txtBuscar, txtEscuela;
    private JComboBox<String> cbEstadoCivil, cbSexo;
    private JButton btnGuardar, btnModificar, btnEliminar, btnSalir, btnBuscar, btnReporteEstudiantes, btnReporteEstados;
    private JTable table;
    private DefaultTableModel tableModel;

    public StudentApp() {
        setTitle("Registro de Estudiantes");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(15, 2)); // Cambiar de 14 a 15 para acomodar el nuevo botón
        panel.setBackground(new Color(173, 216, 230)); 

        panel.add(createLabel("Nombre:"));
        txtNombre = new JTextField();
        panel.add(txtNombre);

        panel.add(createLabel("Apellido:"));
        txtApellido = new JTextField();
        panel.add(txtApellido);

        panel.add(createLabel("C.I.:"));
        txtCI = new JTextField();
        txtCI.setDocument(new JTextFieldLimit(8));
        panel.add(txtCI);

        panel.add(createLabel("Edad:"));
        txtEdad = new JTextField();
        panel.add(txtEdad);

        panel.add(createLabel("Estado Civil:"));
        cbEstadoCivil = new JComboBox<>(new String[]{"Soltero", "Casado", "Divorciado", "Viudo"});
        panel.add(cbEstadoCivil);

        panel.add(createLabel("Sexo:"));
        cbSexo = new JComboBox<>(new String[]{"Masculino", "Femenino"});
        panel.add(cbSexo);

        panel.add(createLabel("Estado:"));
        txtEstado = new JTextField();
        panel.add(txtEstado);

        panel.add(createLabel("Ciudad:"));
        txtCiudad = new JTextField();
        panel.add(txtCiudad);

        panel.add(createLabel("N° Escuela:"));
        txtEscuela = new JTextField();
        txtEscuela.setDocument(new JTextFieldLimit(2));
        panel.add(txtEscuela);

        btnGuardar = createButton("Guardar", new Color(60, 179, 113), e -> guardarEstudiante());
        panel.add(btnGuardar);

        btnModificar = createButton("Modificar", new Color(255, 165, 0), e -> modificarEstudiante());
        panel.add(btnModificar);

        btnEliminar = createButton("Eliminar", new Color(255, 69, 0), e -> eliminarEstudiante());
        panel.add(btnEliminar);

        btnSalir = createButton("Salir", new Color(0, 191, 255), e -> System.exit(0));
        panel.add(btnSalir);

        txtBuscar = new JTextField();
        panel.add(txtBuscar);

        btnBuscar = createButton("Buscar", new Color(30, 144, 255), e -> buscarEstudiante());
        panel.add(btnBuscar);

        panel.add(createLabel("Buscar:"));

        btnReporteEstudiantes = createButton("Reporte Estudiantes", new Color(75, 0, 130), e -> imprimirReporte("reportes/estudiantes.jasper"));
        panel.add(btnReporteEstudiantes);

        btnReporteEstados = createButton("Reporte por Estados", new Color(75, 0, 130), e -> imprimirReporte("reportes/Estados1.jasper"));
        panel.add(btnReporteEstados);

        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        container.add(panel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"Nombre", "Apellido", "C.I.", "Edad", "Estado Civil", "Sexo", "Estado", "Ciudad", "N° Escuela"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        container.add(scrollPane, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                if (!event.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                    cargarDatosSeleccionados(table.getSelectedRow());
                }
            }
        });

        cargarEstudiantes();
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(25, 25, 112)); 
        return label;
    }

    private JButton createButton(String text, Color background, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.addActionListener(actionListener);
        return button;
    }

    private void imprimirReporte(String reportePath) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            // Obtener la URL del recurso
            URL resourceUrl = getClass().getClassLoader().getResource(reportePath);

            // Verificar si la URL es null
            if (resourceUrl == null) {
                JOptionPane.showMessageDialog(this, "El archivo " + reportePath + " no se encontró en el classpath.");
                return;
            }

            // Obtener la ruta del archivo
            String reportPath = Paths.get(resourceUrl.toURI()).toString();
            System.out.println("Ruta del reporte: " + reportPath); // Para depuración

            // Cargar el reporte ya compilado
            JasperReport reporte = (JasperReport) JRLoader.loadObjectFromFile(reportPath);

            // Rellenar el reporte
            JasperPrint impresion = JasperFillManager.fillReport(reporte, new HashMap<>(), conn);

            // Mostrar el reporte
            JasperViewer.viewReport(impresion, false);
        } catch (JRException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al generar el reporte: " + ex.getMessage());
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error de base de datos: " + ex.getMessage());
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al acceder al archivo del reporte: " + ex.getMessage());
        } finally {
            // Cerrar la conexión a la base de datos si no es nula
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void guardarEstudiante() {
        if (!validarCampos()) {
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sqlDireccion = "INSERT INTO Direcciones (estado, ciudad) VALUES (?, ?)";
            PreparedStatement psDireccion = conn.prepareStatement(sqlDireccion, Statement.RETURN_GENERATED_KEYS);
            psDireccion.setString(1, txtEstado.getText());
            psDireccion.setString(2, txtCiudad.getText());
            psDireccion.executeUpdate();
            ResultSet rsDireccion = psDireccion.getGeneratedKeys();
            rsDireccion.next();
            int idDireccion = rsDireccion.getInt(1);

            String sqlEstudiante = "INSERT INTO Estudiantes (nombre, apellido, ci, edad, id_direccion, estado_civil, sexo, numero_escuela) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement psEstudiante = conn.prepareStatement(sqlEstudiante);
            psEstudiante.setString(1, txtNombre.getText());
            psEstudiante.setString(2, txtApellido.getText());
            psEstudiante.setString(3, txtCI.getText());
            psEstudiante.setInt(4, Integer.parseInt(txtEdad.getText()));
            psEstudiante.setInt(5, idDireccion);
            psEstudiante.setString(6, cbEstadoCivil.getSelectedItem().toString());
            psEstudiante.setString(7, cbSexo.getSelectedItem().toString());
            psEstudiante.setInt(8, Integer.parseInt(txtEscuela.getText()));
            psEstudiante.executeUpdate();

            JOptionPane.showMessageDialog(this, "Estudiante guardado exitosamente.");
            cargarEstudiantes(); 
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al guardar el estudiante.");
        }
    }

    private void modificarEstudiante() {
        if (!validarCampos()) {
            return;
        }

        int response = JOptionPane.showConfirmDialog(this, "¿Está seguro de modificar este estudiante?", "Confirmar Modificación", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.NO_OPTION) {
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sqlEstudiante = "UPDATE Estudiantes SET nombre = ?, apellido = ?, edad = ?, estado_civil = ?, sexo = ?, numero_escuela = ? WHERE ci = ?";
            PreparedStatement psEstudiante = conn.prepareStatement(sqlEstudiante);
            psEstudiante.setString(1, txtNombre.getText());
            psEstudiante.setString(2, txtApellido.getText());
            psEstudiante.setInt(3, Integer.parseInt(txtEdad.getText()));
            psEstudiante.setString(4, cbEstadoCivil.getSelectedItem().toString());
            psEstudiante.setString(5, cbSexo.getSelectedItem().toString());
            psEstudiante.setInt(6, Integer.parseInt(txtEscuela.getText()));
            psEstudiante.setString(7, txtCI.getText());
            psEstudiante.executeUpdate();

            String sqlDireccion = "UPDATE Direcciones SET estado = ?, ciudad = ? WHERE id = (SELECT id_direccion FROM Estudiantes WHERE ci = ?)";
            PreparedStatement psDireccion = conn.prepareStatement(sqlDireccion);
            psDireccion.setString(1, txtEstado.getText());
            psDireccion.setString(2, txtCiudad.getText());
            psDireccion.setString(3, txtCI.getText());
            psDireccion.executeUpdate();

            JOptionPane.showMessageDialog(this, "Estudiante modificado exitosamente.");
            cargarEstudiantes(); 
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al modificar el estudiante.");
        }
    }

    private void eliminarEstudiante() {
        int response = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar este estudiante?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.NO_OPTION) {
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sqlEstudiante = "DELETE FROM Estudiantes WHERE ci = ?";
            PreparedStatement psEstudiante = conn.prepareStatement(sqlEstudiante);
            psEstudiante.setString(1, txtCI.getText());
            psEstudiante.executeUpdate();

            String sqlDireccion = "DELETE FROM Direcciones WHERE id = (SELECT id_direccion FROM Estudiantes WHERE ci = ?)";
            PreparedStatement psDireccion = conn.prepareStatement(sqlDireccion);
            psDireccion.setString(1, txtCI.getText());
            psDireccion.executeUpdate();

            JOptionPane.showMessageDialog(this, "Estudiante eliminado exitosamente.");
            cargarEstudiantes(); 
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al eliminar el estudiante.");
        }
    }

    private void buscarEstudiante() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT e.*, d.estado, d.ciudad FROM Estudiantes e INNER JOIN Direcciones d ON e.id_direccion = d.id WHERE e.ci = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtBuscar.getText());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtNombre.setText(rs.getString("nombre"));
                txtApellido.setText(rs.getString("apellido"));
                txtCI.setText(rs.getString("ci"));
                txtEdad.setText(rs.getString("edad"));
                txtEstado.setText(rs.getString("estado"));
                txtCiudad.setText(rs.getString("ciudad"));
                cbEstadoCivil.setSelectedItem(rs.getString("estado_civil"));
                cbSexo.setSelectedItem(rs.getString("sexo"));
                txtEscuela.setText(rs.getString("numero_escuela"));
            } else {
                JOptionPane.showMessageDialog(this, "Estudiante no encontrado.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al buscar el estudiante.");
        }
    }

    private boolean validarCampos() {
        if (txtNombre.getText().isEmpty() || txtApellido.getText().isEmpty() || txtCI.getText().isEmpty() || txtEdad.getText().isEmpty() || txtEstado.getText().isEmpty() || txtCiudad.getText().isEmpty() || txtEscuela.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos.");
            return false;
        }
        return true;
    }

    private void cargarDatosSeleccionados(int rowIndex) {
        txtNombre.setText(tableModel.getValueAt(rowIndex, 0).toString());
        txtApellido.setText(tableModel.getValueAt(rowIndex, 1).toString());
        txtCI.setText(tableModel.getValueAt(rowIndex, 2).toString());
        txtEdad.setText(tableModel.getValueAt(rowIndex, 3).toString());
        cbEstadoCivil.setSelectedItem(tableModel.getValueAt(rowIndex, 4).toString());
        cbSexo.setSelectedItem(tableModel.getValueAt(rowIndex, 5).toString());
        txtEstado.setText(tableModel.getValueAt(rowIndex, 6).toString());
        txtCiudad.setText(tableModel.getValueAt(rowIndex, 7).toString());
        txtEscuela.setText(tableModel.getValueAt(rowIndex, 8).toString());
    }

    private void cargarEstudiantes() {
        tableModel.setRowCount(0); // Limpiar la tabla

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT e.*, d.estado, d.ciudad FROM Estudiantes e INNER JOIN Direcciones d ON e.id_direccion = d.id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("ci"),
                        rs.getInt("edad"),
                        rs.getString("estado_civil"),
                        rs.getString("sexo"),
                        rs.getString("estado"),
                        rs.getString("ciudad"),
                        rs.getInt("numero_escuela")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar los estudiantes.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new StudentApp().setVisible(true);
        });
    }
}
