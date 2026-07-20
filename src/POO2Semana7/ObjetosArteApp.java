package POO2Semana7;
 
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;
 
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
 
/**
 * Aplicacion de escritorio para la gestion de una coleccion de objetos de
 * arte (obras). Implementa las operaciones CRUD (Crear, Leer, Actualizar y
 * Eliminar) sobre la tabla ObjetosArte de una base de datos MySQL local,
 * utilizando Java Swing para la interfaz grafica y JDBC para la conexion
 * con la base de datos.
 */
public class ObjetosArteApp {
 
    private Connection connection;
 
    private JFrame frame;
    private JTextField idField;
    private JTextField nombreField;
    private JTextField autorField;
    private JTextField fechaField;
    private JTextField estiloField;
    private JButton actualizarButton;
    private JButton borrarButton;
 
    // Constructor: orquesta la creacion de la ventana, la conexion a la
    // base de datos y el montaje de la interfaz grafica.
    public ObjetosArteApp() {
        initialize();
        connectToDatabase();
        createGUI();
    }
 
    // Inicializa la ventana principal (JFrame) de la aplicacion.
    private void initialize() {
        frame = new JFrame("Objetos de Arte App");
        frame.setBounds(100, 100, 950, 420);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
 
    // Establece la conexion JDBC con la base de datos MySQL local.
    // Maneja los errores de conexion mostrando un mensaje al usuario
    // y finalizando la aplicacion de forma controlada.
    private void connectToDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
 
            String url = "jdbc:mysql://localhost:3306/Semana7";
            String user = "root";
            String password = "gato2281";
 
            connection = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame,
                    "Error al conectar a la base de datos. Verifique que el servicio de MySQL "
                            + "se encuentre activo y que los datos de conexion sean correctos.",
                    "Error de conexion", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
 
    // Construye la interfaz grafica: panel de formulario, botones,
    // area de resultados y los listeners de eventos correspondientes.
    private void createGUI() {
 
        JPanel panel = new JPanel(new GridLayout(10, 1, 4, 4));
        JPanel panel2 = new JPanel(new BorderLayout());
 
        // setPreferredSize (JComponent): define un tamano uniforme para el
        // panel del formulario, independiente del contenido que reciba.
        panel.setPreferredSize(new Dimension(300, 360));
        // setBackground (JComponent): entrega consistencia visual al panel.
        panel.setBackground(new Color(245, 245, 245));
 
        frame.getContentPane().add(panel, BorderLayout.WEST);
        frame.getContentPane().add(panel2, BorderLayout.CENTER);
 
        // Campos de entrada
        JLabel idLabel = new JLabel("ID:");
        idField = new JTextField();
        idField.setToolTipText("Numero entero unico que identifica la obra");
 
        JLabel nombreLabel = new JLabel("Nombre:");
        nombreField = new JTextField();
        nombreField.setToolTipText("Nombre de la obra de arte");
 
        JLabel autorLabel = new JLabel("Autor:");
        autorField = new JTextField();
        autorField.setToolTipText("Nombre del autor de la obra");
 
        JLabel fechaLabel = new JLabel("Fecha de Creación:");
        fechaField = new JTextField();
        // setToolTipText (JComponent): orienta al usuario sobre el formato
        // de fecha esperado por la base de datos (tipo DATE).
        fechaField.setToolTipText("Formato requerido: aaaa-mm-dd (ej: 1889-06-01)");
 
        JLabel estiloLabel = new JLabel("Estilo Artístico:");
        estiloField = new JTextField();
        estiloField.setToolTipText("Corriente o estilo artistico de la obra");
 
        // Botones de las operaciones CRUD
        JButton consultarButton = new JButton("Consultar");
        JButton insertarButton = new JButton("Insertar");
        actualizarButton = new JButton("Actualizar");
        borrarButton = new JButton("Borrar");
 
        // setForeground / setBackground (JComponent): estilo visual propio
        // para diferenciar la accion de consulta del resto de operaciones.
        consultarButton.setBackground(new Color(0, 105, 92));
        consultarButton.setForeground(Color.WHITE);
 
        // setEnabled (JComponent): Actualizar y Borrar solo tienen sentido
        // una vez que existe un registro cargado en el formulario, por lo
        // que se deshabilitan hasta que el usuario seleccione uno desde
        // el area de resultados.
        actualizarButton.setEnabled(false);
        borrarButton.setEnabled(false);
 
        // Area de texto para mostrar los resultados de las consultas
        JTextArea resultTextArea = new JTextArea(10, 45);
        resultTextArea.setEditable(false);
 
        // Accion del boton Consultar
        consultarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                consultarObjetos(resultTextArea);
            }
        });
 
        // Accion del boton Insertar
        insertarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                insertarObjeto();
                consultarObjetos(resultTextArea);
            }
        });
 
        // Accion del boton Actualizar
        actualizarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actualizarObjeto();
                consultarObjetos(resultTextArea);
            }
        });
 
        // Accion del boton Borrar
        borrarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                borrarObjeto();
                consultarObjetos(resultTextArea);
            }
        });
 
        // Agregar componentes al panel del formulario
        panel.add(idLabel);
        panel.add(idField);
        panel.add(nombreLabel);
        panel.add(nombreField);
        panel.add(autorLabel);
        panel.add(autorField);
        panel.add(fechaLabel);
        panel.add(fechaField);
        panel.add(estiloLabel);
        panel.add(estiloField);
        panel.add(consultarButton);
        panel.add(insertarButton);
        panel.add(actualizarButton);
        panel.add(borrarButton);
 
        // addMouseListener (JComponent): permite detectar el clic del
        // usuario sobre un registro del area de resultados y cargar sus
        // datos en el formulario para su edicion o eliminacion.
        resultTextArea.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
            	int offset = resultTextArea.viewToModel2D(e.getPoint());
                try {
                    int rowStart = Utilities.getRowStart(resultTextArea, offset);
                    int rowEnd = Utilities.getRowEnd(resultTextArea, offset);
                    String selectedText = resultTextArea.getText(rowStart, rowEnd - rowStart);
                    if (!selectedText.trim().isEmpty()) {
                        cargarDatosSeleccionados(selectedText);
                        actualizarButton.setEnabled(true);
                        borrarButton.setEnabled(true);
                    }
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        });
 
        panel2.add(new JScrollPane(resultTextArea));
 
        frame.setVisible(true);
 
        // Carga inicial de los registros existentes al abrir la aplicacion
        consultarObjetos(resultTextArea);
    }
 
    // Crear (Create): inserta un nuevo objeto de arte en la base de datos
    // a partir de los datos ingresados en el formulario.
    private void insertarObjeto() {
        try {
            int id = Integer.parseInt(idField.getText().trim());
            String nombre = nombreField.getText().trim();
            String autor = autorField.getText().trim();
            String fecha = fechaField.getText().trim();
            String estilo = estiloField.getText().trim();
 
            String sql = "INSERT INTO ObjetosArte (ID, Nombre, Autor, FechaCreacion, EstiloArtistico) "
                    + "VALUES (?, ?, ?, ?, ?)";
 
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            statement.setString(2, nombre);
            statement.setString(3, autor);
            statement.setDate(4, Date.valueOf(fecha));
            statement.setString(5, estilo);
            statement.executeUpdate();
 
            JOptionPane.showMessageDialog(frame, "Objeto de arte insertado con éxito", "Éxito",
            		JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error al insertar el objeto de arte en la base de datos",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame,
                    "Datos invalidos. Verifique que el ID sea numerico y que la fecha tenga el formato aaaa-mm-dd",
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
        }
    }
 
    // Leer (Read): consulta todos los registros de la tabla ObjetosArte y
    // los muestra en el area de resultados.
    private void consultarObjetos(JTextArea resultTextArea) {
        try {
            String sql = "SELECT * FROM ObjetosArte ORDER BY ID";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
 
            resultTextArea.setText("");
            while (resultSet.next()) {
                int id = resultSet.getInt("ID");
                String nombre = resultSet.getString("Nombre");
                String autor = resultSet.getString("Autor");
                Date fecha = resultSet.getDate("FechaCreacion");
                String estilo = resultSet.getString("EstiloArtistico");
 
                resultTextArea.append("ID: " + id + ", Nombre: " + nombre + ", Autor: " + autor
                        + ", Fecha de Creación: " + fecha + ", Estilo Artístico: " + estilo + "\n");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error al consultar los objetos de arte", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
 
    // Carga en los campos del formulario los datos del registro sobre el
    // cual el usuario hizo clic en el area de resultados.
    private void cargarDatosSeleccionados(String selectedText) {
        String[] parts = selectedText.split(", ");
        for (String part : parts) {
            String[] keyValue = part.split(": ", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                switch (key) {
                    case "ID":
                        idField.setText(value);
                        break;
                    case "Nombre":
                        nombreField.setText(value);
                        break;
                    case "Autor":
                        autorField.setText(value);
                        break;
                    case "Fecha de Creación":
                        fechaField.setText(value);
                        break;
                    case "Estilo Artístico":
                        estiloField.setText(value);
                        break;
                }
            }
        }
    }
 
    // Actualizar (Update): modifica en la base de datos el registro cuyo
    // ID corresponde al del formulario, con los nuevos valores ingresados.
    private void actualizarObjeto() {
        try {
            int id = Integer.parseInt(idField.getText().trim());
            String nombre = nombreField.getText().trim();
            String autor = autorField.getText().trim();
            String fecha = fechaField.getText().trim();
            String estilo = estiloField.getText().trim();
 
            String sql = "UPDATE ObjetosArte SET Nombre = ?, Autor = ?, FechaCreacion = ?, "
                    + "EstiloArtistico = ? WHERE ID = ?";
 
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, nombre);
            statement.setString(2, autor);
            statement.setDate(3, Date.valueOf(fecha));
            statement.setString(4, estilo);
            statement.setInt(5, id);
            statement.executeUpdate();
 
            JOptionPane.showMessageDialog(frame, "Objeto de arte actualizado con éxito", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error al actualizar el objeto de arte", "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame,
                    "Datos invalidos. Verifique que el ID sea numerico y que la fecha tenga el formato aaaa-mm-dd",
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
        }
    }
 
    // Eliminar (Delete): elimina de la base de datos el registro cuyo ID
    // corresponde al ingresado (o seleccionado) en el formulario.
    private void borrarObjeto() {
        try {
            int id = Integer.parseInt(idField.getText().trim());
 
            String sql = "DELETE FROM ObjetosArte WHERE ID = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            statement.executeUpdate();
 
            JOptionPane.showMessageDialog(frame, "Objeto de arte eliminado con éxito", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error al eliminar el objeto de arte", "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Ingrese un ID numerico valido para eliminar",
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
        }
    }
 
    // Limpia los campos del formulario y deshabilita Actualizar/Borrar
    // hasta que se seleccione un nuevo registro.
    private void limpiarCampos() {
        idField.setText("");
        nombreField.setText("");
        autorField.setText("");
        fechaField.setText("");
        estiloField.setText("");
        actualizarButton.setEnabled(false);
        borrarButton.setEnabled(false);
    }
 
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ObjetosArteApp());
    }
}