package br.com.royalfarma.database;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import br.com.royalfarma.interfaces.IFetchProducts;
import br.com.royalfarma.interfaces.ILoginInfos;
import br.com.royalfarma.interfaces.iCEPHelper;
import br.com.royalfarma.interfaces.iOrderStatus;
import br.com.royalfarma.model.Endereco;
import br.com.royalfarma.model.Produto;
import br.com.royalfarma.model.Usuario;
import br.com.royalfarma.utils.BuscaCEPHelper;

import static br.com.royalfarma.utils.Util.MY_LOG_TAG;
import static br.com.royalfarma.utils.Util.getSha512FromString;
import static br.com.royalfarma.utils.Util.unmask;
import static java.sql.Types.NULL;

public class DataBaseConnection {
    //    private static final String driverFireBird = "net.sourceforge.jtds.jdbc.Driver";
    public static Connection connection;
    private static Handler handler;
    public static final Object lock = new Object();

    public DataBaseConnection(Handler handler) {
        DataBaseConnection.handler = handler;
    }

    /**
     * [20:49, 31/03/2020] +55 31 9696-8905:  www.royalfarma.com.br
     * [20:50, 31/03/2020] +55 31 9696-8905: usuario royalfar_root
     * [20:50, 31/03/2020] +55 31 9696-8905: senha raphaelle01
     */

    @NonNull
    public static void createConnection() {
        synchronized (lock) {
            try {
                if (connection == null || connection.isClosed() || !connection.isValid(500)) {
                    try {
                        Message message = new Message();
                        message.what = 0;
                        message.obj = "tentando_conectar";
                        handler.sendMessage(message);

                        Class.forName("com.mysql.jdbc.Driver");
                        String dbName = "royalfar_db";
                        String dbUserName = "royalfar_root";
                        String dbPassword = "raphaelle01";
                        String connectionString = "jdbc:mysql://www.royalfarma.com.br:3306/" + dbName + "?user=" + dbUserName + "&password=" + dbPassword + "&useUnicode=true&characterEncoding=UTF-8";

                        connection = DriverManager.getConnection(connectionString);

                        Log.v(MY_LOG_TAG, "####################################");
                        Log.i(MY_LOG_TAG, "Conexão ao Banco criada com Sucesso!");
                        Log.v(MY_LOG_TAG, "####################################");

                        lock.notify();
                    } catch (SQLException | ClassNotFoundException e) {
                        Log.e(MY_LOG_TAG, "Falhou ao criar conexão com o Banco! -> " + e.getMessage());
                        e.printStackTrace();
                        lock.notify();
                    }
                } else {
                    lock.notify();
                }
            } catch (SQLException e) {
                lock.notify();
                e.printStackTrace();
            }
        }
    }

    public static boolean checkConnection() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Classes Async do App
     **/
    public static class FetchProducts extends AsyncTask<Void, Void, ArrayList<Produto>> {
        private final Handler handler;
        private final IFetchProducts iFetchProducts;
        private final boolean limited;
        private final String limit;
        private Message message;

        public FetchProducts(Handler handler, boolean limited, String limit, IFetchProducts iFetchProducts) {
            this.iFetchProducts = iFetchProducts;
            this.handler = handler;
            this.limited = limited;
            this.limit = limit;
        }

        @Override
        protected void onPreExecute() {
            Log.v(MY_LOG_TAG, "### ON PRE EXECUTE ###");
            this.message = new Message();
            this.message.what = 1;
            message.obj = "onPreExecute";
            handler.sendMessage(message);
            super.onPreExecute();
        }

        @Override
        protected ArrayList<Produto> doInBackground(Void... voids) {
            try {
                synchronized (lock) {
                    new Thread(DataBaseConnection::createConnection).start();
                    lock.wait();
                    Log.v(MY_LOG_TAG, "doInBackground");
                    this.message = new Message();
                    this.message.what = 1;

                    String sqlGetHomePdtsNovidades = "SELECT " +
                            "pdt_id, " +
                            "pdt_code, " +
                            "pdt_name, " +
                            "pdt_title, " +
                            "pdt_inventory, " +
                            "pdt_cover, " +
                            "pdt_offer_price, " +
                            "pdt_offer_start, " +
                            "pdt_offer_end, " +
                            "pdt_price " +
                            "FROM  ws_products " +
                            "WHERE pdt_status = 1 " +
                            "AND (pdt_inventory >= 1 OR pdt_inventory IS NULL) " +
                            "AND (pdt_offer_end IS NULL OR pdt_offer_end < NOW() OR TIMEDIFF(pdt_offer_end, CURRENT_TIMESTAMP()) > '24:00:00') " +
                            "ORDER BY " +
                            "RAND() ";
                    StringBuilder sb = new StringBuilder(sqlGetHomePdtsNovidades);
                    if (limited) sb.append("LIMIT ").append(limit);

                    PreparedStatement pstmt = connection.prepareStatement(sb.toString());
                    ResultSet resultSet = pstmt.executeQuery();

                    ArrayList<Produto> todosOsProdutos = new ArrayList<>();
                    Produto produto;
                    while (resultSet.next()) {
                        String id = resultSet.getString("pdt_id");
                        String codBarra = resultSet.getString("pdt_code");
                        String titulo = resultSet.getString("pdt_title");
                        String pathToImage = resultSet.getString("pdt_cover");
                        String preco = resultSet.getString("pdt_price");
                        String estoque = resultSet.getString("pdt_inventory");
                        String precoOferta = resultSet.getString("pdt_offer_price");
                        String inicioOferta = resultSet.getString("pdt_offer_start");
                        String fimOferta = resultSet.getString("pdt_offer_end");

                        produto = new Produto();
                        produto.setImagemURL(pathToImage);
                        produto.setId(Integer.parseInt(id));
                        produto.setNome(titulo);
                        produto.setPreco(Double.parseDouble(preco.replace(", ", ".")));
                        produto.setCodBarra(Long.parseLong(codBarra));
                        produto.setEstoqueAtual(Integer.parseInt(estoque));
                        if (precoOferta != null && !precoOferta.equals("NULL") && !precoOferta.equals("")) {
                            produto.setDesconto(true);
                            produto.setPrecoOferta(Double.parseDouble(precoOferta.replace(", ", ".")));
                            produto.setInicioOferta(inicioOferta);
                            produto.setFimOferta(fimOferta);
                        }
                        todosOsProdutos.add(produto);
                    }

                    //                    listaDeCategorias.add(produtosNovidades);
//                    listaDeCategorias.add(produtosMaisVendidos);
                    return todosOsProdutos;
                }
            } catch (SQLException e) {
                Log.e(MY_LOG_TAG, "Erro de SQL");
                message.obj = "erro_sql";
                handler.sendMessage(message);
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        //        @Override
//        protected void onProgressUpdate(Integer... progress) {
//            Log.d(MY_LOG_TAG, "### ON PROGRESS UPDATE ###");
//            super.onProgressUpdate(progress);
//        }

        @Override
        protected void onPostExecute(ArrayList<Produto> todosOsProdutos) {
            this.message = new Message();
            this.message.what = 1;
            Log.v(MY_LOG_TAG, "### ON POST EXECUTE ###");
            message.obj = "onPostExecute";
            handler.sendMessage(message);
            //let HomeFragment know that we are done
            iFetchProducts.onGetDataDone(todosOsProdutos);
        }
    }

    public static class FetchProductsByQuery extends AsyncTask<String, Integer, ArrayList<Produto>> {
        private final Handler handler;
        private final IFetchProducts iFetchProducts;
        private final boolean limited;
        private final int limit;
        private Message message;

        public FetchProductsByQuery(Handler handler, boolean limited, int limit, IFetchProducts iFetchProducts) {
            this.iFetchProducts = iFetchProducts;
            this.handler = handler;
            this.limited = limited;
            this.limit = limit;
        }

        @Override
        protected void onPreExecute() {
            Log.v(MY_LOG_TAG, "### ON PRE EXECUTE ###");
            this.message = new Message();
            this.message.what = 1;
            this.message.obj = "onPreExecute";
            this.handler.sendMessage(message);
            super.onPreExecute();
        }

        @Override
        protected ArrayList<Produto> doInBackground(String... strings) {
            try {
                synchronized (lock) {
                    new Thread(DataBaseConnection::createConnection).start();
                    lock.wait();
                    Log.v(MY_LOG_TAG, "doInBackground");
                    this.message = new Message();
                    this.message.what = 1;

                    String sqlGetHomePdtsNovidades = "SELECT " +
                            "pdt_id, " +
                            "pdt_code, " +
                            "pdt_name, " +
                            "pdt_title, " +
                            "pdt_inventory, " +
                            "pdt_cover, " +
                            "pdt_offer_price, " +
                            "pdt_offer_start, " +
                            "pdt_offer_end, " +
                            "pdt_price " +
                            "FROM  ws_products " +
                            "WHERE pdt_status = 1 " +
                            "AND (pdt_inventory >= 1 OR pdt_inventory IS NULL) " +
                            "AND (pdt_name LIKE ? OR pdt_code LIKE ?) " +
                            "AND (pdt_offer_end IS NULL OR pdt_offer_end < NOW() OR TIMEDIFF(pdt_offer_end, CURRENT_TIMESTAMP()) > '24:00:00') " +
                            "ORDER BY " +
                            "RAND() ";
                    StringBuilder sb = new StringBuilder(sqlGetHomePdtsNovidades);
                    if (limited)
                        sb.append("LIMIT ").append(limit);

                    PreparedStatement pstmt = connection.prepareStatement(sb.toString());
                    pstmt.setString(1, "%" + strings[0] + "%");
                    pstmt.setString(2, "%" + strings[0] + "%");

                    ResultSet resultSet = pstmt.executeQuery();


                    ArrayList<Produto> todosOsProdutos = new ArrayList<>();
                    Produto produto;

                    this.message.obj = "size: " + resultSet.getFetchSize() + 1;
                    Log.d(MY_LOG_TAG, "Size: " + this.message.obj);
                    this.handler.sendMessage(message);
                    int progress = 0;
                    while (resultSet.next()) {
                        this.message = new Message();
                        this.message.what = 1;
                        this.message.obj = "progress: " + ++progress;
                        handler.sendMessage(message);

                        String id = resultSet.getString("pdt_id");
                        String codBarra = resultSet.getString("pdt_code");
                        String titulo = resultSet.getString("pdt_title");
                        String pathToImage = resultSet.getString("pdt_cover");
                        String preco = resultSet.getString("pdt_price");
                        String estoque = resultSet.getString("pdt_inventory");
                        String precoOferta = resultSet.getString("pdt_offer_price");
                        String inicioOferta = resultSet.getString("pdt_offer_start");
                        String fimOferta = resultSet.getString("pdt_offer_end");

                        produto = new Produto();
                        produto.setImagemURL(pathToImage);
                        produto.setId(Integer.parseInt(id));
                        produto.setNome(titulo);
                        produto.setPreco(Double.parseDouble(preco.replace(", ", ".")));
                        produto.setCodBarra(Long.parseLong(codBarra));
                        produto.setEstoqueAtual(Integer.parseInt(estoque));
                        if (precoOferta != null && !precoOferta.equals("NULL") && !precoOferta.equals("")) {
                            produto.setDesconto(true);
                            produto.setPrecoOferta(Double.parseDouble(precoOferta.replace(", ", ".")));
                            produto.setInicioOferta(inicioOferta);
                            produto.setFimOferta(fimOferta);
                        }
                        todosOsProdutos.add(produto);
                    }

                    //                    listaDeCategorias.add(produtosNovidades);
//                    listaDeCategorias.add(produtosMaisVendidos);
                    return todosOsProdutos;
                }
            } catch (SQLException e) {
                Log.e(MY_LOG_TAG, "Erro de SQL");
                message.obj = "erro_sql";
                handler.sendMessage(message);
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        //        @Override
//        protected void onProgressUpdate(Integer... progress) {
//            Log.d(MY_LOG_TAG, "### ON PROGRESS UPDATE ###");
//            super.onProgressUpdate(progress);
//        }

        @Override
        protected void onPostExecute(ArrayList<Produto> todosOsProdutos) {
            this.message = new Message();
            this.message.what = 1;
            Log.v(MY_LOG_TAG, "### ON POST EXECUTE ###");
            message.obj = "onPostExecute";
            handler.sendMessage(message);
            //let HomeFragment know that we are done
            iFetchProducts.onGetDataDone(todosOsProdutos);
        }
    }

    public static class GetClientInfo extends AsyncTask<String, Void, Usuario> {
        private final Handler handler;
        private Message message;
        private ILoginInfos iLoginInfos;

        public GetClientInfo(Handler handler, ILoginInfos iLoginInfos) {
            this.handler = handler;
            this.iLoginInfos = iLoginInfos;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.v(MY_LOG_TAG, "onPreExecute");
            this.message = new Message();
            this.message.what = 1;
            message.obj = "onPreExecute";
            handler.sendMessage(message);
        }

        @Override
        protected Usuario doInBackground(String... params) {
            try {
                synchronized (lock) {
                    new Thread(DataBaseConnection::createConnection).start();
                    lock.wait();

                    Log.v(MY_LOG_TAG, "doInBackground");
                    this.message = new Message();
                    this.message.what = 1;
                    String login = params[0];
                    String pass = getSha512FromString(params[1]);
                    if (pass == null) {
                        Log.e(MY_LOG_TAG, "Erro de Sha-512");
                        message.obj = "erro_sql";
                        handler.sendMessage(message);
                        return null;
                    }
                    try {
                        String sqlAuthenticate = "SELECT " +
                                "ws_users.user_id, " +
                                "ws_users.user_password " +
                                "FROM ws_users " +
                                "WHERE " +
                                "ws_users.user_email = ?";
                        PreparedStatement pstmt = connection.prepareStatement(sqlAuthenticate);
                        pstmt.setString(1, login);
                        ResultSet resultSet = pstmt.executeQuery();

                        //Pega result set e compara informações
                        if (resultSet.next()) {
                            String dbPass = resultSet.getString("user_password");
                            if (dbPass.equals(pass)) {
                                Usuario cliente = new Usuario(login, pass);
                                cliente.setId(resultSet.getInt("ws_users.user_id"));
                                String sqlGetAddrInfos = "SELECT ws_users_address.addr_id, " +
                                        "ws_users_address.addr_name, " +
                                        "ws_users_address.addr_zipcode, " +
                                        "ws_users_address.addr_street, " +
                                        "ws_users_address.addr_number, " +
                                        "ws_users_address.addr_complement, " +
                                        "ws_users_address.addr_district, " +
                                        "ws_users_address.addr_city, " +
                                        "ws_users_address.addr_state, " +
                                        "ws_users_address.addr_country " +
                                        "FROM ws_users " +
                                        "JOIN ws_users_address " +
                                        "ON ws_users.user_id = ws_users_address.user_id " +
                                        "WHERE " +
                                        "ws_users_address.user_id = ?";

                                pstmt = connection.prepareStatement(sqlGetAddrInfos);
                                pstmt.setInt(1, cliente.getId());
                                resultSet = pstmt.executeQuery();
                                //inicia no -1
                                if (resultSet.getFetchSize() + 1 >= 0) {
                                    ArrayList<Endereco> enderecos = new ArrayList<>();
                                    while (resultSet.next()) {
                                        Endereco endereco = new Endereco();
                                        endereco.setAddrID(resultSet.getInt("ws_users_address.addr_id"));
                                        endereco.setAddrName(resultSet.getString("ws_users_address.addr_name"));
                                        endereco.setAddrZipCode(resultSet.getString("ws_users_address.addr_zipcode"));
                                        endereco.setAddrComplement(resultSet.getString("ws_users_address.addr_complement"));
                                        endereco.setAddrStreet(resultSet.getString("ws_users_address.addr_street"));
                                        endereco.setAddrNumber(resultSet.getString("ws_users_address.addr_number"));
                                        endereco.setAddrDistrict(resultSet.getString("ws_users_address.addr_district"));
                                        endereco.setAddrCity(resultSet.getString("ws_users_address.addr_city"));
                                        endereco.setAddrState(resultSet.getString("ws_users_address.addr_state"));
                                        endereco.setAddrCountry(resultSet.getString("ws_users_address.addr_country"));
                                        enderecos.add(endereco);
                                    }
                                    cliente.setEnderecos(enderecos);
                                    Log.i(MY_LOG_TAG, "Autenticado com sucesso [Com Endereço]");
                                    message.obj = "autenticado";
                                    handler.sendMessage(message);
                                    return cliente;
                                } else {
                                    message.obj = "autenticado";
                                    handler.sendMessage(message);
                                    Log.i(MY_LOG_TAG, "Autenticado com sucesso");
                                    Log.i(MY_LOG_TAG, "Cliente sem endereço");
                                    return cliente;
                                }

                            } else {
                                message.obj = "erro_autenticacao";
                                handler.sendMessage(message);
                                Log.e(MY_LOG_TAG, "Erro na Autenticação");
                            }
                        } else {
                            message.obj = "erro_autenticacao";
                            handler.sendMessage(message);
                            Log.e(MY_LOG_TAG, "Erro na Autenticação");
                        }
                    } catch (SQLException e) {
                        Log.e(MY_LOG_TAG, "Erro de SQL");
                        message.obj = "erro_sql";
                        handler.sendMessage(message);
                        e.printStackTrace();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Usuario infosCliente) {
            this.message = new Message();
            this.message.what = 1;
            Log.v(MY_LOG_TAG, "onPostExecute");
            message.obj = "onPostExecute";
            handler.sendMessage(message);
            iLoginInfos.onInfosLoginResult(infosCliente);
            super.onPostExecute(infosCliente);
        }
    }

    public static class InsertClientAddress extends AsyncTask<Usuario, Void, Void> {
        private final Handler handler;
        private final Endereco endereco;
        private final Usuario usuario;
        private Message message;

        public InsertClientAddress(Handler handler, Usuario usuario, Endereco endereco) {
            this.endereco = endereco;
            this.usuario = usuario;
            this.handler = handler;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.v(MY_LOG_TAG, "onPreExecute");
            this.message = new Message();
            this.message.what = 1;
            message.obj = "onPreExecute";
            handler.sendMessage(message);
        }

        @Override
        protected Void doInBackground(Usuario... usuarios) {
            try {
                synchronized (lock) {
                    new Thread(DataBaseConnection::createConnection).start();
                    lock.wait();

                    Log.v(MY_LOG_TAG, "doInBackground");
                    this.message = new Message();
                    this.message.what = 1;
                    if (usuario == null) {
                        Log.e(MY_LOG_TAG, "Erro ao resgatar usuario");
                        message.obj = "usuario_null";
                        handler.sendMessage(message);
                        return null;
                    }

                    String name = endereco.getAddrName();
                    String zipCode = endereco.getAddrZipCode();
                    String street = endereco.getAddrStreet();
                    String number = endereco.getAddrNumber();
                    String complement = endereco.getAddrComplement();
                    String district = endereco.getAddrDistrict();
                    String city = endereco.getAddrCity();
                    String state = endereco.getAddrState();

                    String sqlInsertAddress = "INSERT INTO ws_users_address VALUES (?, NULL, NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    String sqlUpdateAddress = "UPDATE ws_users_address " +
                            "SET " +
                            "user_id = ?, " +
                            "addr_key = NULL, " +
                            "addr_name = ?, " +
                            "addr_zipcode = ?, " +
                            "addr_street = ?, " +
                            "addr_number = ?, " +
                            "addr_complement = ?, " +
                            "addr_district = ?, " +
                            "addr_city = ?, " +
                            "addr_state = ?, " +
                            "addr_country = ? " +
                            "WHERE " +
                            "addr_id = ?";

                    PreparedStatement pstmt;
                    int addrID = endereco.getAddrID();
                    if (addrID == 0) {
                        pstmt = connection.prepareStatement(sqlInsertAddress, PreparedStatement.RETURN_GENERATED_KEYS);
                    } else {
                        pstmt = connection.prepareStatement(sqlUpdateAddress);
                    }
                    pstmt.setInt(1, usuario.getId());
                    pstmt.setString(2, name);
                    pstmt.setString(3, zipCode);
                    pstmt.setString(4, street);
                    pstmt.setString(5, number);
                    pstmt.setString(6, complement);
                    pstmt.setString(7, district);
                    pstmt.setString(8, city);
                    pstmt.setString(9, state);
                    pstmt.setString(10, "Brasil");
                    if (addrID != 0) {
                        pstmt.setInt(11, addrID);
                    }

                    int affectedLines = pstmt.executeUpdate();

                    if (addrID != 0 && affectedLines == 1) {
                        message.obj = "atualizado";
                        handler.sendMessage(message);
                    } else if (addrID == 0 && affectedLines == 1) {
                        ResultSet rs = pstmt.getGeneratedKeys();
                        int key = rs.next() ? rs.getInt(1) : 0;
                        if (key != 0) {
                            message.obj = "inserido_" + key;
                            handler.sendMessage(message);
                            Log.d(MY_LOG_TAG, "Generated key=" + key);
                        } else {
                            message.obj = "falhou_inserir";
                            handler.sendMessage(message);
                            Log.e(MY_LOG_TAG, "Erro ao Resgatar Key Generated");
                        }

                    } else {
                        message.obj = "falhou_inserir";
                        handler.sendMessage(message);
                        Log.e(MY_LOG_TAG, "Erro ao Inserir Endereço");
                    }
                }
            } catch (SQLException | InterruptedException e) {
                Log.e(MY_LOG_TAG, "Erro de SQL");
                message.obj = "erro_sql";
                handler.sendMessage(message);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            this.message = new Message();
            this.message.what = 1;
            Log.v(MY_LOG_TAG, "onPostExecute");
            message.obj = "onPostExecute";
            handler.sendMessage(message);
            super.onPostExecute(voids);
        }
    }

    public static class InsertPurchase extends AsyncTask<Usuario, Void, Void> {
        private final Handler handler;
        private final ArrayList<String> infosCompra;
        private final ArrayList<Produto> produtos;
        private Message message;
        private int orderID;

        public InsertPurchase(Handler handler, ArrayList<String> infosCompra, ArrayList<Produto> produtos) {
            this.infosCompra = infosCompra;
            this.produtos = produtos;
            this.handler = handler;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.v(MY_LOG_TAG, "onPreExecute");
            this.message = new Message();
            this.message.what = 1;
            message.obj = "onPreExecute";
            handler.sendMessage(message);
        }

        @Override
        protected Void doInBackground(Usuario... usuarios) {
            try {
                synchronized (lock) {
                    new Thread(DataBaseConnection::createConnection).start();
                    lock.wait();

                    Log.v(MY_LOG_TAG, "onPreExecuteInsereOrdem");
                    this.message = new Message();
                    this.message.what = 1;
                    this.message.obj = "onPreExecuteInsereOrdem";
                    handler.sendMessage(message);
                    if (infosCompra == null) {
                        Log.e(MY_LOG_TAG, "Erro ao resgatar Infos De Compra");
                        message.obj = "infos_compra_null";
                        handler.sendMessage(message);
                        return null;
                    } else {
                        String insertOrder = "INSERT INTO ws_orders VALUES (" +
                                "?, " + //userid
                                "?, " + //order id
                                "?, " + //order status
                                "?, " + //order payment
                                "?, " + //valorcompra order price
                                "?, " + //order installments
                                "?, " + //instalmment
                                "?, " + //coupon
                                "?, " + //free
                                "?, " + //billet
                                "?, " + //code
                                "?, " + //addrId
                                "?, " + //shipcode
                                "?, " + //shiprice (frete)
                                "CURRENT_DATE(), " + //data envio
                                "?, " + //order tracking
                                "?, " + //nfepdf
                                "?, " + //nfexml
                                "CURRENT_TIMESTAMP(), " + //order date
                                "CURRENT_TIMESTAMP(), " + //order update
                                "?, " + //mail processing
                                "?, " + //completed
                                "?)"; //troco
                        PreparedStatement pstmt;
//                            pstmt = connection.prepareStatement(insertOrder);
                        pstmt = connection.prepareStatement(insertOrder, PreparedStatement.RETURN_GENERATED_KEYS);
                        pstmt.setInt(1, Integer.parseInt(infosCompra.get(0))); //id
                        pstmt.setInt(2, NULL); //order id
                        pstmt.setInt(3, 3); //status 3 = novo pedido
                        pstmt.setString(4, infosCompra.get(1)); //payment
                        pstmt.setBigDecimal(5, new BigDecimal(infosCompra.get(2))); //price
                        pstmt.setBigDecimal(6, new BigDecimal(infosCompra.get(3))); //installment
                        pstmt.setBigDecimal(7, new BigDecimal(infosCompra.get(4))); //installments
                        pstmt.setInt(8, NULL); //coupon
                        pstmt.setInt(9, NULL); //free
                        pstmt.setInt(10, NULL); //billet
                        pstmt.setInt(11, NULL); //code
                        pstmt.setInt(12, Integer.parseInt(infosCompra.get(5))); //addrid
                        pstmt.setInt(13, Integer.parseInt(infosCompra.get(6))); //shipcode
                        pstmt.setBigDecimal(14, new BigDecimal("0.00")); //shipprice
                        pstmt.setInt(15, NULL); //ordertackin
                        pstmt.setInt(16, NULL); //nfe
                        pstmt.setInt(17, NULL); //nfe
                        pstmt.setInt(18, NULL); //mail processing
                        pstmt.setInt(19, NULL); //completed
                        pstmt.setBigDecimal(20, new BigDecimal(infosCompra.get(7))); //troco

                        int affectedLines = pstmt.executeUpdate();
                        if (affectedLines == 1) {
                            ResultSet rs = pstmt.getGeneratedKeys();
                            orderID = rs.next() ? rs.getInt(1) : 0;
                            Produto p;
                            for (int i = 0; i < produtos.size(); i++) {
                                String sqlItensPedido = "INSERT INTO ws_orders_items VALUES (?,?,?,?,?,?,?)";
                                p = produtos.get(i);
                                pstmt = connection.prepareStatement(sqlItensPedido);
                                pstmt.setInt(1, orderID); //order_id
                                pstmt.setInt(2, p.getId()); //pdt_id
                                pstmt.setInt(3, NULL);//stock_id
                                pstmt.setInt(4, NULL);//item_id
                                pstmt.setString(5, p.getNome());//item_name
                                if (p.isDesconto()) {
                                    pstmt.setBigDecimal(6, new BigDecimal(p.getPrecoOferta())); //item_price
                                } else {
                                    pstmt.setBigDecimal(6, new BigDecimal(p.getPreco())); //item_price
                                }
                                pstmt.setBigDecimal(7, new BigDecimal(p.getQtdNoCarrinho()));//item_amount

                                if (!pstmt.execute()) {
                                    Message message = new Message();
                                    message.what = 1;
                                    Log.v(MY_LOG_TAG, "ORDER_ID" + orderID);
                                    message.obj = "orderIdValue" + orderID;
                                    handler.sendMessage(message);

                                    Message message2 = new Message();
                                    message2.what = 1;
                                    message2.obj = "pedidoInserido";
                                    handler.sendMessage(message2);
                                } else {
                                    Message message = new Message();
                                    message.what = 1;
                                    message.obj = "falhouInserirPedido";
                                    handler.sendMessage(message);
                                    Log.e(MY_LOG_TAG, "Erro ao Inserir Compra");
                                }
                            }
                        } else {
                            Message message = new Message();
                            message.what = 1;
                            message.obj = "falhouInserirPedido";
                            handler.sendMessage(message);
                            Log.e(MY_LOG_TAG, "Erro ao Inserir Compra");
                        }
                    }
                }
            } catch (SQLException | InterruptedException e) {
                Message message = new Message();
                message.what = 1;
                message.obj = "erro_sql";
                handler.sendMessage(message);
                e.printStackTrace();
                Log.e(MY_LOG_TAG, "Erro de SQL");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            this.message = new Message();
            this.message.what = 1;
            Log.v(MY_LOG_TAG, "onPostExecute");
            message.obj = "onPostExecute";
            handler.sendMessage(message);
            super.onPostExecute(voids);
        }
    }

    public static class GetOrderStatus extends AsyncTask<String, Void, Integer> {
        private final iOrderStatus iOrderStatus;

        public GetOrderStatus(iOrderStatus iOrderStatus) {
            this.iOrderStatus = iOrderStatus;
        }

        @Override
        protected Integer doInBackground(String... strings) {
            try {
                synchronized (lock) {
                    new Thread(DataBaseConnection::createConnection).start();
                    lock.wait();

                    String sqlGetStatus = "SELECT order_status FROM ws_orders WHERE order_id = ?";
                    PreparedStatement pstm = connection.prepareStatement(sqlGetStatus);
                    pstm.setInt(1, Integer.parseInt(strings[0]));
                    ResultSet rs = pstm.executeQuery();
                    if (rs.next()) {
                        return rs.getInt("order_status");
                    }
                }
            } catch (SQLException | InterruptedException e) {
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            if (integer == null) {
                integer = -1;
            }
            iOrderStatus.orderStatus(integer);
            super.onPostExecute(integer);
        }
    }

    public static class ValidateCEPBH extends AsyncTask<String, Void, String> {
        private final iCEPHelper iCEPHelper;
        String city = "";

        public ValidateCEPBH(iCEPHelper iCEPHelper) {
            this.iCEPHelper = iCEPHelper;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                return BuscaCEPHelper.getCidadeByCEP(unmask(strings[0]));
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            iCEPHelper.onCEPResult(s);
            super.onPostExecute(s);
        }
    }
}
