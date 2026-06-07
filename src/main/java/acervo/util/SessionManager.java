package acervo.util;

public class SessionManager {
	
	 private static String nomeAdmin = null;
	 
	 private SessionManager() {}
	 
	 public static void login (String nome) {
		 nomeAdmin = nome;
	 }
	 
	 public static void logout() {
		 nomeAdmin = null;
	 }
	 
	 public static boolean isLogado() {
		 return nomeAdmin != null;
	 }
	 
	 public static String getNomeAdmin() {
		 return nomeAdmin;
	 }
}
