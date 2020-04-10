package br.com.royalfarma.database;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import br.com.royalfarma.interfaces.IFetchHomeProducts;
import br.com.royalfarma.model.Produto;

import static br.com.royalfarma.utils.Util.MY_LOG_TAG;
import static br.com.royalfarma.utils.Util.getSha512FromString;

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
                if (connection == null || connection.isClosed()) {
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
    public static class AuthenticateLogin extends AsyncTask<String, Void, Void> {
        private final Handler handler;
        private Message message;

        public AuthenticateLogin(Handler handler) {
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
        protected Void doInBackground(String... params) {
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
                        String sqlAuthenticate = "SELECT user_password FROM ws_users WHERE user_email = ?";

                        Log.i(MY_LOG_TAG, "IS VALID: " + connection.isValid(3000));

                        PreparedStatement pstmt = connection.prepareStatement(sqlAuthenticate);
                        pstmt.setString(1, login);
                        ResultSet resultSet = pstmt.executeQuery();

                        //Pega result set e compara informações
                        if (resultSet.next()) {
                            String dbPass = resultSet.getString("user_password");
                            if (dbPass.equals(pass)) {
                                message.obj = "autenticado";
                                handler.sendMessage(message);
                                Log.i(MY_LOG_TAG, "Autenticado com sucesso");
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
        protected void onPostExecute(Void aVoid) {
            this.message = new Message();
            this.message.what = 1;
            Log.v(MY_LOG_TAG, "onPostExecute");
            message.obj = "onPostExecute";
            handler.sendMessage(message);
            super.onPostExecute(aVoid);
        }
    }

    public static class FetchHomeProducts extends AsyncTask<Void, Void, ArrayList<ArrayList<Produto>>> {
        private final Handler handler;
        private final IFetchHomeProducts iFetchHomeProducts;
        private Message message;
        private ArrayList<ArrayList<Produto>> listaDeCategorias;

        public FetchHomeProducts(Handler handler, IFetchHomeProducts iFetchHomeProducts) {
            this.iFetchHomeProducts = iFetchHomeProducts;
            this.handler = handler;
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
        protected ArrayList<ArrayList<Produto>> doInBackground(Void... voids) {
            try {
                synchronized (lock) {
                    new Thread(DataBaseConnection::createConnection).start();
                    lock.wait();
                    Log.v(MY_LOG_TAG, "doInBackground");
                    this.message = new Message();
                    this.message.what = 1;

                    String sqlGetHomePdtsNovidades = "SELECT pdt_id, pdt_code, pdt_title, pdt_cover, pdt_price, pdt_inventory FROM ws_products WHERE pdt_category IS NULL AND pdt_cover IS NOT NULL LIMIT 5";
//                    String sqlGetHomePdtsNovidades = "SELECT pdt_id, pdt_code, pdt_title, pdt_cover, pdt_price, pdt_inventory FROM ws_products WHERE pdt_category IS NULL LIMIT 5";
//                    String getHomeProductsPopulares = "SELECT TOP 5 user_password FROM ws_users WHERE user_email = ?";
//                    String getHomeProductsMaisVendidos = "SELECT TOP 5 user_password FROM ws_users WHERE user_email = ?";

                    PreparedStatement pstmt = connection.prepareStatement(sqlGetHomePdtsNovidades);
//                    pstmt.setString(1, "CATEGORIA");
//                    pstmt.setString(1, String.valueOf(NULL));
                    ResultSet resultSet = pstmt.executeQuery();
                    listaDeCategorias = new ArrayList<>();
                    ArrayList<Produto> produtosNovidades = new ArrayList<>();
                    ArrayList<Produto> produtosPopulares = new ArrayList<>();
                    ArrayList<Produto> produtosMaisVendidos = new ArrayList<>();
                    while (resultSet.next()) {
                        String id = resultSet.getString("pdt_id");
                        String codBarra = resultSet.getString("pdt_code");
                        String titulo = resultSet.getString("pdt_title");
                        String pathToImage = resultSet.getString("pdt_cover");
                        String preco = resultSet.getString("pdt_price");
                        String qtdDisponivel = resultSet.getString("pdt_inventory");
//                        public Produto(int id, String titulo, double valor, long codBarra, int Estoque) {
                        Produto produto = new Produto(pathToImage, Integer.parseInt(id), titulo, Double.parseDouble(preco.replace(", ", ".")), Long.parseLong(codBarra), Integer.parseInt(qtdDisponivel));
                        produtosPopulares.add(produto);
                        produtosNovidades.add(produto);
                        produtosMaisVendidos.add(produto);
                    }
                    listaDeCategorias.add(produtosPopulares);
                    listaDeCategorias.add(produtosNovidades);
                    listaDeCategorias.add(produtosMaisVendidos);
                    return listaDeCategorias;
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
        protected void onPostExecute(ArrayList<ArrayList<Produto>> listaDeProdutos) {
            this.message = new Message();
            this.message.what = 1;
            Log.v(MY_LOG_TAG, "### ON POST EXECUTE ###");
            message.obj = "onPostExecute";
            handler.sendMessage(message);
            //let HomeFragment know that we are done
            iFetchHomeProducts.updateHomeWithData(listaDeProdutos);
        }
    }
}
