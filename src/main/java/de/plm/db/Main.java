package de.plm.db;

import de.plm.db.entity.AttachmentsEntity;
import de.plm.db.entity.FieldsEntity;
import de.plm.db.entity.TasksEntity;
import de.plm.db.entity.VehiclesEntity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.plaf.nimbus.State;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Main {

    private static String TRACTOR = "DVC-2";
    private static int TRACTOR_ID = 4;
    private static int TVT_ID = 1;

    private static HashMap<String, AttachmentsEntity> attachments;
    private static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
//        String filePath = "D:\\Daten\\Dokumente\\PLM\\VarioDoc\\23-08-23\\230827_185128_exported_TaskData0_10\\TASKDATA\\TASKDATA.XML";
//        String filePath = "D:\\Daten\\Dokumente\\PLM\\VarioDoc\\23-09-09\\230909_203730_exported_TaskData0_9\\TASKDATA\\TASKDATA.XML";
//        String filePath = "D:\\Daten\\Dokumente\\PLM\\VarioDoc\\23-09-17\\230917_140831_exported_TaskData0_9\\TASKDATA\\TASKDATA.XML";
//        String filePath = "D:\\Daten\\Dokumente\\PLM\\VarioDoc\\23-10-01\\231001_102815_exported_TaskData0_12\\TASKDATA\\TASKDATA.XML";
//        String filePath = "D:\\Daten\\Dokumente\\PLM\\VarioDoc\\23-10-15\\231015_150042_exported_TaskData0_6\\TASKDATA\\TASKDATA.XML";

        String[] filePathes = new String[]{
                "D:\\Daten\\Dokumente\\PLM\\VarioDoc\\23-08-23\\230827_185128_exported_TaskData0_10\\TASKDATA\\TASKDATA.XML",
                "D:\\Daten\\Dokumente\\PLM\\VarioDoc\\23-09-09\\230909_203730_exported_TaskData0_9\\TASKDATA\\TASKDATA.XML",
                "D:\\Daten\\Dokumente\\PLM\\VarioDoc\\23-09-17\\230917_140831_exported_TaskData0_9\\TASKDATA\\TASKDATA.XML",
                "D:\\Daten\\Dokumente\\PLM\\VarioDoc\\23-10-01\\231001_102815_exported_TaskData0_12\\TASKDATA\\TASKDATA.XML",
                "D:\\Daten\\Dokumente\\PLM\\VarioDoc\\23-10-15\\231015_150042_exported_TaskData0_6\\TASKDATA\\TASKDATA.XML",
                "D:\\Daten\\Dokumente\\PLM\\VarioDoc\\24-01-16\\240116_150714_exported_TaskData0_4\\TASKDATA\\TASKDATA.XML",
                "D:\\Daten\\Dokumente\\PLM\\VarioDoc\\24-02-06\\240206_151941_exported_TaskData0_2\\TASKDATA\\TASKDATA.XML"
        };

        try {
            String piIp = "192.168.178.66";

            String url = "jdbc:mariadb://" + piIp + "/plm";

            String name = "root";
            String password = "root";

            System.out.println("Connecting..");
            Connection connection = DriverManager.getConnection(url, name, password);
            System.out.println(connection);
            Statement statement = connection.createStatement();

            loadAttachments(statement);

            String date = "24-10-09";//"24-08-25";//"24-08-14";//"24-07-28";//"24-07-06";//"24-06-07";//"24-05-23";//"24-04-23";//"24-04-10";//"24-03-11";
            String OUTPUT_DIR = "C:\\Users\\steff\\OneDrive\\PLM\\Aufträge\\Trimble\\" + date + "\\out";
            String fileName = "241009_134626_exported_TaskData0_22";

            importVario(statement, "C:\\Users\\steff\\OneDrive\\PLM\\Aufträge\\VarioDoc\\" + date + "\\" + fileName + "\\TASKDATA\\TASKDATA.XML");
            //importTrimble(statement, OUTPUT_DIR);

            // OLD

            //String date = "24-02-06";
            //String OUTPUT_DIR = "D:\\Daten\\Dokumente\\PLM\\Trimble\\" + date + "\\out";

            //importTrimble(statement, OUTPUT_DIR);
            //importTrimble(statement, "D:\\Daten\\Dokumente\\PLM\\Trimble ALL (till 16.01.24)\\out");
//            for (String filePath : filePathes)
//                importVario(statement, filePath);

            //am ende die unnötigen attachments entfernen
            String[] deletableNames = {"Amazone UX Rüben", "Anhängespritzmaschine test", "Horsch Joker Halb", "Horsch Joker t7(8cm links)", "Schneidwerk CR9070 9m", "Test 2.5m", "Test 9m"};
            for(String delete : deletableNames)
                statement.execute("DELETE FROM attachments WHERE name='" + delete + "'");

        } catch (Exception ex) {
        }
    }

    private static void loadAttachments(Statement statement) throws SQLException {
        attachments = new HashMap<>();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM attachments");
        while (resultSet.next()) {
            AttachmentsEntity attachment = new AttachmentsEntity();
            attachment.setId(resultSet.getInt("id"));
            attachment.setName(resultSet.getString("name"));
            attachment.setWidth(resultSet.getDouble("width"));
            attachments.put(attachment.getName(), attachment);
        }
    }

    public static void importTrimble(Statement statement, String filePath) {
        String fieldsPath = filePath + "\\fields";
        String tasksPath = filePath + "\\tasks";
        String attachmentsPath = filePath + "\\implements";

        try {
            File folder = new File(fieldsPath);

            HashMap<String, FieldsEntity> trimbleFields = new HashMap<>();

            FieldsEntity EMPTY_FIELD = new FieldsEntity();
            EMPTY_FIELD.setId(1);
            EMPTY_FIELD.setName("Empty");
            EMPTY_FIELD.setSize(0.0);

            statement.execute("INSERT IGNORE INTO vehicles (id, name) VALUES (1, 'New Holland TVT')");
            statement.executeQuery("INSERT IGNORE INTO fields (id, name, size) VALUES (1, 'Empty', 0.0)");

            AttachmentsEntity prontoAttachment = attachments.get("Horsch Pronto");
            AttachmentsEntity zamAttachment = attachments.get("Amazone ZAM");
            AttachmentsEntity uxAttachment = attachments.get("Amazone UX");
            AttachmentsEntity uniaAttachment = attachments.get("Unia Max");
            AttachmentsEntity jokerAttachment = attachments.get("Horsch Joker");
            AttachmentsEntity terranoAttachment = attachments.get("Horsch Terrano");
            AttachmentsEntity schneidwerkAttachment = attachments.get("Schneidwerk CR9070");
            AttachmentsEntity mulcherAttachment = attachments.get("Mulcher");
            AttachmentsEntity pflugAttachment = attachments.get("Lemken Variopal");
            AttachmentsEntity unicornAttachment = attachments.get("Kverneland Unicorn");
            //TODO add unicornAttachment

//
//            query = statement.executeQuery("SELECT * FROM attachments WHERE name='ZAM'");
//            query.next();
//            int zamId = query.getInt("id");

            //fields
            for (File file : folder.listFiles()) {
                File fieldFile = new File(fieldsPath + "\\" + file.getName() + "\\" + file.getName() + ".xml");

                try {
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.newDocumentBuilder();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document doc = db.parse(fieldFile);
                    doc.getDocumentElement().normalize();

                    Element root = doc.getDocumentElement();

                    FieldsEntity field = new FieldsEntity();
                    trimbleFields.put(root.getAttributes().getNamedItem("uuid").getNodeValue(), field);

                    NodeList nodes = root.getChildNodes();
                    //TODO try to load

                    for (int i = 0; i < nodes.getLength(); i++) {
                        Node child = nodes.item(i);
                        if (child.getNodeName().equals("guidance") || child.getNodeName().equals("#text") || child.getNodeName().equals("boundaries") || child.getNodeName().equals("landmarks"))
                            continue;

                        if (child.getNodeName().equals("name")) field.setName(child.getTextContent());
                    }

                    saveField(statement, field);
                    System.out.println(field.getName() + " size: " + field.getSize());
                } catch (ParserConfigurationException | IOException | SAXException e) {
                    throw new RuntimeException(e);
                }
            }

            folder = new File(attachmentsPath);
            //attachments
            if(folder.exists()) {
                System.out.println(folder.listFiles().length);
                for (File file : folder.listFiles()) {
                    File attachmentFile = new File(attachmentsPath + "\\" + file.getName() + "\\" + file.getName() + ".xml");
                    try {
                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        dbf.newDocumentBuilder();
                        DocumentBuilder db = dbf.newDocumentBuilder();
                        Document doc = db.parse(attachmentFile);
                        doc.getDocumentElement().normalize();

                        Element root = doc.getDocumentElement();

                        double width = 0.0;
                        String attachmentName = "";

                        AttachmentsEntity attachment = new AttachmentsEntity();
                        attachment.setWidth(width);
                        attachment.setName(attachmentName);

                        NodeList nodes = root.getChildNodes();

                        for (int i = 0; i < nodes.getLength(); i++) {
                            Node child = nodes.item(i);
                            if (child.getNodeName().equals("guidance") || child.getNodeName().equals("#text") || child.getNodeName().equals("boundaries") || child.getNodeName().equals("landmarks"))
                                continue;

                            if (child.getNodeName().equals("name")) attachment.setName(child.getTextContent());
                            if (child.getNodeName().equals("width"))
                                attachment.setWidth(Double.parseDouble(child.getTextContent()));
                        }

                        statement.execute("INSERT IGNORE INTO attachments (name, width) VALUES ('" + attachment.getName() + "', " + attachment.getWidth() + ")");
                    } catch (Exception ignored) {

                    }
                }
            }

            folder = new File(tasksPath);
            for (File file : folder.listFiles()) {
                File attachmentFile = new File(attachmentsPath + "\\" + file.getName() + "\\" + file.getName() + ".xml");
                try {
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.newDocumentBuilder();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document doc = db.parse(attachmentFile);
                    doc.getDocumentElement().normalize();

                    Element root = doc.getDocumentElement();

                    double width = 0.0;
                    String attachmentName = "";

                    AttachmentsEntity attachment = new AttachmentsEntity();
                    attachment.setWidth(width);
                    attachment.setName(attachmentName);

                    NodeList nodes = root.getChildNodes();

                    for (int i = 0; i < nodes.getLength(); i++) {
                        Node child = nodes.item(i);
                        if (child.getNodeName().equals("guidance") || child.getNodeName().equals("#text") || child.getNodeName().equals("boundaries") || child.getNodeName().equals("landmarks"))
                            continue;

                        if (child.getNodeName().equals("name")) attachment.setName(child.getTextContent());
                        if (child.getNodeName().equals("width"))
                            attachment.setWidth(Double.parseDouble(child.getTextContent()));
                    }

                    statement.execute("INSERT IGNORE INTO attachments (name, width) VALUES ('" + attachment.getName() + "', " + attachment.getWidth() + ")");
                } catch (Exception ignored) {

                }
            }
            System.out.println(folder);

            //tasks
            for (File file : folder.listFiles()) {
                File taskFolder = new File(file.getAbsolutePath());
                List<File> files = Arrays.asList(taskFolder.listFiles());
                files.sort((f1, f2) -> f2.getName().compareTo(f1.getName()));

                File taskinfo = files.get(0);

                try {
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.newDocumentBuilder();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document doc = db.parse(taskinfo);
                    doc.getDocumentElement().normalize();

                    Element root = doc.getDocumentElement();
                    Node taskElement = root.getFirstChild();

                    TasksEntity task = new TasksEntity();

                    task.setVehiclesId(TVT_ID);

                    AttachmentsEntity empty = new AttachmentsEntity();
                    ResultSet rs = statement.executeQuery("SELECT * FROM attachments WHERE name='Amazone UX'");
                    if (rs.next())
                        empty.setId(rs.getInt("id"));
                    else empty.setId(1);

                    //TODO schöner machen und infos casten

//                  System.out.println(taskElement.getNodeName());
//                  System.out.println(root.getNodeName());
                    NodeList childs = root.getChildNodes();
                    for (int i = 0; i < childs.getLength(); i++) {
                        Node child = childs.item(i);
                        if (child.getNodeName().equalsIgnoreCase("#text"))
                            continue;

                        if (child.getNodeName().equals("name")) {
//                            System.out.println(child.getNodeName() + " " + child.getTextContent());
                            task.setDescription(child.getTextContent());
                            String taskDesc = task.getDescription().toLowerCase();

                            task.setAttachment(empty);
                            if (taskDesc.contains("spri")) task.setAttachment(uxAttachment);
                            if (taskDesc.contains("streuen") || taskDesc.contains("ssa") || taskDesc.contains("harnstoff") || taskDesc.contains("stabur"))
                                task.setAttachment(zamAttachment);
                            if (taskDesc.contains("vorarbei")) task.setAttachment(uniaAttachment);
                            if (taskDesc.contains("joker")) task.setAttachment(jokerAttachment);
                            if (taskDesc.contains("grubber") || taskDesc.contains("terrano"))
                                task.setAttachment(terranoAttachment);
                            if (taskDesc.contains("ernte") || taskDesc.contains("mäh")) task.setAttachment(schneidwerkAttachment);
                            if (taskDesc.contains("mulch")) task.setAttachment(mulcherAttachment);
                            if (taskDesc.contains("saat") || taskDesc.contains("aussaat") || taskDesc.toLowerCase().contains("aussat") || taskDesc.contains("gerste") || taskDesc.contains("weizen") || taskDesc.contains("raps") || taskDesc.contains("zwfr") || taskDesc.contains("grenze"))
                                task.setAttachment(prontoAttachment);
                            if (taskDesc.contains("pflug") || taskDesc.contains("pflüg"))
                                task.setAttachment(pflugAttachment);
                            if(taskDesc.contains("bepflanzung"))
                                task.setAttachment(unicornAttachment);
                        }

                        if (child.getNodeName().equals("fields")) {
                            NodeList subChilds = child.getChildNodes();
                            for (int j = 0; j < subChilds.getLength(); j++) {
                                if (!subChilds.item(j).getNodeName().equals("field")) continue;
                                System.out.println(subChilds.item(j).getTextContent());
                                FieldsEntity field = trimbleFields.get(subChilds.item(j).getTextContent());
                                task.setField(field);

                                break;
                            }
                        }
                        if (task.getField() == null) {
                            task.setField(EMPTY_FIELD);
                            System.out.println(task.getField());
                        }

                        if (child.getNodeName().equals("time")) {
                            String timeString = child.getTextContent().replace("T", " ").replace("Z", "");
                            timeString = timeString.substring(0, timeString.length() - 3);
//                            System.out.println(timeString);
                            LocalDateTime begin = LocalDateTime.parse(timeString, FORMATTER);
                            task.setBegin(begin);
                        }

                        if (child.getNodeName().equals("properties")) {
                            NodeList propertyNodes = child.getChildNodes();
                            for (int j = propertyNodes.getLength() - 1; j >= 0; j--) {
                                Node property = propertyNodes.item(j);
                                if (property.getNodeName().equalsIgnoreCase("#text")) continue;

                                Node attribute = property.getAttributes().item(0);
                                switch (attribute.getTextContent()) {
                                    case "runtime":
                                        String runtimeString = property.getTextContent();
                                        if (runtimeString.endsWith(".0"))
                                            runtimeString = runtimeString.replace(".0", "");
                                        task.setDuration(Long.parseLong(runtimeString) / 1000);

                                        task.setEnd(task.getBegin().plusSeconds(task.getDuration()));
                                        break;
                                    case "operation":
                                        System.out.println("TODO parse machines");

                                        break;
                                    case "created":
                                        LocalDateTime date = Instant.ofEpochMilli(Long.parseLong(property.getTextContent())).atZone(ZoneId.systemDefault()).toLocalDateTime();
                                        task.setBegin(date);
                                        //System.out.println(Long.parseLong(property.getTextContent()) + " " + date);
                                        break;
                                    default:

                                }
                            }
                        }

                    }

                    if (task.getDuration() <= 0) continue;
                    saveTask(statement, task);
                    //System.out.println(task.getDescription() + " " + task.getField().getName() + " " + task.getAttachment() + " " + task.getDuration() + " " + task.getBegin() + " " + task.getEnd());
                } catch (ParserConfigurationException | IOException | SAXException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void importVario(Statement statement, String filePath) {
        try {
            VehiclesEntity fendt = new VehiclesEntity();
            fendt.setId(2);
            fendt.setName("Fendt 936");

            statement.execute("INSERT IGNORE INTO vehicles (id, name) VALUES (2, 'Fendt 936')");

            File file = new File(filePath);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();

            HashMap<String, FieldsEntity> fieldMap = new HashMap<>();
            HashMap<String, AttachmentsEntity> attachmentMap = new HashMap<>();

            NodeList childs = root.getChildNodes();
            for (int i = childs.getLength() - 1; i >= 0; i--) {
                Node node = childs.item(i);
                String nodeName = node.getNodeName();
                if (!nodeName.equalsIgnoreCase("#text")) {
                    NamedNodeMap attributes = node.getAttributes();
                    if (nodeName.equalsIgnoreCase("PFD")) {
                        FieldsEntity field = new FieldsEntity();
                        field.setName(attributes.getNamedItem("C").getNodeValue());
                        field.setSize(Double.parseDouble(attributes.getNamedItem("D").getNodeValue()));
                        fieldMap.put(attributes.getNamedItem("A").getNodeValue(), field);

                        saveField(statement, field);
//                        statement.execute("INSERT IGNORE INTO fields (name, size) VALUES ('" + field.getName() + "', " + field.getSize() + ");");

                        ResultSet result = statement.executeQuery("SELECT * FROM fields WHERE name='" + field.getName() + "';");
                        result.next();
                        field.setId(result.getInt("id"));
                    }

                    if (nodeName.equalsIgnoreCase("DVC")) {
                        AttachmentsEntity attachment = new AttachmentsEntity();
                        attachment.setName(attributes.getNamedItem("B").getNodeValue());
                        if (attachment.getName().equalsIgnoreCase("dri") || attachment.getName().equalsIgnoreCase("fendt vario tractor"))
                            continue;
                        for (AttachmentsEntity attach : attachments.values()) {
                            if (attach.getName().toLowerCase().contains(attachment.getName().toLowerCase()) && !attach.getName().toLowerCase().contains("(")) {
                                System.out.println(attachment.getName() + " is " + attach.getName());
                                attachment = attach;
                                break;
                            }
                        }

                        NodeList nodeList = node.getChildNodes();
                        for (int j = 0; j < nodeList.getLength(); j++) {
                            Node child = nodeList.item(j);
                            if (child.getNodeName().equalsIgnoreCase("DPT") &&
                                    (child.getAttributes().getNamedItem("A").getNodeValue().equalsIgnoreCase("11") || child.getAttributes().getNamedItem("A").getNodeValue().equalsIgnoreCase("10"))) {
                                double width = Double.parseDouble(child.getAttributes().getNamedItem("C").getNodeValue());
                                attachment.setWidth(width / 1000.0);
                            }
                        }

                        attachmentMap.put(node.getAttributes().getNamedItem("A").getNodeValue(), attachment);
                        statement.execute("INSERT IGNORE INTO attachments (name, width) VALUES ('" + attachment.getName() + "', " + attachment.getWidth() + ")");

                        ResultSet result = statement.executeQuery("SELECT * FROM attachments WHERE name='" + attachment.getName().replace("Horsch_ISO_Saemaschine", "Horsch Pronto") + "';");
                        result.next();
                        attachment.setId(result.getInt("id"));
                    }

                    if (nodeName.equalsIgnoreCase("TSK")) {
                        TasksEntity task = new TasksEntity();

                        Node varioIdNode = node.getAttributes().getNamedItem("E");
                        if (varioIdNode == null) continue;

                        String varioId = varioIdNode.getNodeValue();
                        FieldsEntity field = fieldMap.get(varioId);

                        NodeList nodeList = node.getChildNodes();
                        String attachmentVario = null;

                        for (int j = 0; j < nodeList.getLength(); j++) {
                            Node child = nodeList.item(j);
                            if (child.getNodeName().equalsIgnoreCase("DAN")) {
                                attachmentVario = child.getAttributes().getNamedItem("C").getNodeValue();
                                if (!attachmentVario.equalsIgnoreCase(TRACTOR))
                                    break;
                            }
                        }

                        LocalDateTime begin = null, end = null;
                        long total = 0;

                        for (int j = 0; j < nodeList.getLength(); j++) {
                            Node child = nodeList.item(j);
                            if (child.getNodeName().equalsIgnoreCase("TIM")) {
                                if(child.getAttributes().getNamedItem("C") == null) continue;

                                long duration = Long.parseLong(child.getAttributes().getNamedItem("C").getNodeValue());
                                total += duration;
                                String timeString = child.getAttributes().getNamedItem("A").getNodeValue().replace("T", " ");
                                LocalDateTime temp = LocalDateTime.parse(timeString, FORMATTER);

                                if (begin == null || begin.isAfter(temp)) begin = temp;
                                if (end == null || end.isBefore(temp)) end = temp;
                            }
                        }

                        AttachmentsEntity attachment = attachmentMap.get(attachmentVario);

                        task.setField(field);
                        task.setAttachment(attachment);
                        task.setDescription(node.getAttributes().getNamedItem("B").getNodeValue());
                        task.setDuration(total);
                        task.setBegin(begin);
                        task.setEnd(end);
                        task.setVehiclesId(fendt.getId());

                        System.out.println(task.getDescription());

                        saveTask(statement, task);
                    }
                }
            }
        } catch (SQLException | SAXException | IOException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private static void saveField(Statement statement, FieldsEntity field) {
        try {
            statement.execute("INSERT IGNORE INTO fields (name, size) VALUES ('" + field.getName() + "', " + field.getSize() + ");");

            ResultSet result = statement.executeQuery("SELECT * FROM fields WHERE name='" + field.getName() + "';");
            result.next();
            field.setId(result.getInt("id"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void saveTask(Statement statement, TasksEntity task) {
        System.out.println(task.getDescription() + " Task field: " + task.getField() + " Field Name: " + task.getField().getName() + ", " + task.getField().getId());
        System.out.println(task.getDescription() + " " + task.getDuration() + ", " + task.getField().getId() + " (" + task.getFieldsId() + ") from " + task.getBegin() + " to " + task.getEnd());

        try {
            FieldsEntity fe = task.getField();
            statement.execute("INSERT INTO tasks (fields_id, vehicles_id, attachments_id, description, duration, begin, end) VALUES (" + task.getField().getId() + ", " + task.getVehiclesId() + ", " + task.getAttachment().getId() + ", '" + task.getDescription() + "', " + task.getDuration() + ", '" + task.getBegin() + "', '" + task.getEnd() + "')");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static AttachmentsEntity getAttachment(String name, Statement statement) {
        try {
            AttachmentsEntity attachment = new AttachmentsEntity();

            ResultSet query = statement.executeQuery("SELECT * FROM attachments WHERE name='" + name + "'");
            query.next();
            int prontoId = query.getInt("id");
            double prontoWidth = query.getDouble("width");

            attachment.setName("Pronto");
            attachment.setId(prontoId);
            attachment.setWidth(prontoWidth);

            return attachment;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
