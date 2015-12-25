package controllers;

import models.UserAccount;

public class Login {

		public String email;
		public String password;
		public String redirecturl;

		public String validate() {
			if (UserAccount.authenticate(email, password) == null) {
				return "Invalid user or password";
			}
			return null;
		}
	}