package com.jlava.webapp.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import com.jlava.service.RoleManager;
import com.jlava.model.Person;
import com.jlava.model.Role;
import com.jlava.model.RolesForm;

@Controller
@RequestMapping("/roles")
public class RolesController {
	private RoleManager roleManager;

	public RolesController(RoleManager roleManager) {
		this.roleManager = roleManager;
	}

	@RequestMapping(method=GET)
	public String loadHome(ModelMap model) {
		List<Role> roles = roleManager.listRoles();
		
		RolesForm rolesForm = new RolesForm();
		rolesForm.setRoles(roles);

		model.addAttribute("rolesForm", rolesForm);

		return "roles";
	}

	@RequestMapping(value="/populate_role_list", method=GET)
	public @ResponseBody List<Role> getRoleList(ModelMap model){
		return roleManager.listRoles();
	}

	@RequestMapping(value="/populate_person_role", method=GET)
	public @ResponseBody List<Person> filterByRole(@RequestParam("filterBy") Long filterBy, ModelMap model){
		return roleManager.getPersonsWithRole(filterBy);
	}

	@RequestMapping(method=POST)
	public String updateRoles(@ModelAttribute("rolesForm") RolesForm rolesForm, ModelMap model){
		boolean updated = false;

		List<Role> roles = rolesForm.getRoles();	

		for(Role role : new ArrayList<Role>(roles)) {
			if(role.getRoleDesc() != null || !role.getRoleDesc().isEmpty()) {
				roleManager.updateRole(role);
				if(role.isDeleted()) {
					roles.remove(role);
				}

				if(!updated) updated = true;
			}
		}

		if(updated) {
			model.addAttribute("updateResult", "Update Successful");
		}	

		return "roles";
	}

	@RequestMapping(params="addNewRole", method=POST)
	public String addRole(@RequestParam("newCode") String newCode, @RequestParam("newRole") String newRole, RedirectAttributes redirectAttributes){
		String goodAdd = "";
		String badAdd = "";

		if(newCode != null && newRole != null 
				&& !newRole.isEmpty() && !newCode.isEmpty()) {
			if(roleManager.validCodeDesc(newCode, newRole)) {
				roleManager.addRole(newCode, newRole);	
				goodAdd = "New Role Added"; 
			} else {
				badAdd = "Role already exist";
			}
		} else {
			badAdd = "Invalid Code or Description.";
		}

		redirectAttributes.addFlashAttribute("goodAdd", goodAdd);
		redirectAttributes.addFlashAttribute("badAdd", badAdd);
		//redirect to get
		return "redirect:roles";
	}
}