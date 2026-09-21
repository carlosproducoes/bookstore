package br.com.fatecmogidascruzes.bookstore.controllers;

import br.com.fatecmogidascruzes.bookstore.services.ClientService;
import org.springframework.stereotype.Controller;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Controller
public class ClientController {
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/admin/clients")
    public String index(String code, String name, String cpf, String email, String status, Model model) {
        model.addAttribute("clients", clientService.findAll(code, name, cpf, email, status));
        return "admin/clients/index";
    }

    @GetMapping("/admin/clients/new")
    public String create(Model model) {
        model.addAttribute("clientForm", new ClientForm());
        return "admin/clients/new";
    }

    @PostMapping("/admin/clients")
    public String store(@Valid @ModelAttribute("clientForm") ClientForm form,
                        BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/clients/new";
        }

        try {
            clientService.create(form.name(), form.gender(), form.birth(), form.cpf(), form.email(),
                    form.phoneType(), form.ddd(), form.phoneNumber(), form.password(), form.passwordConfirmation(),
                    form.deliveryAddress().toData(), form.sameAddress() ? form.deliveryAddress().toData() : form.billingAddress().toData());
        } catch (IllegalArgumentException exception) {
            model.addAttribute("formError", exception.getMessage());
            return "admin/clients/new";
        }
        return "redirect:/admin/clients";
    }

    @GetMapping("/admin/clients/{id}")
    public String details(@PathVariable String id, Model model) {
        model.addAttribute("client", clientService.findByCode(id));
        model.addAttribute("passwordForm", new PasswordForm(null, null));
        return "admin/clients/details";
    }

    @PostMapping("/admin/clients/{id}/inactivate")
    public String inactivate(@PathVariable String id) {
        clientService.inactivate(id);
        return "redirect:/admin/clients/" + id;
    }

    @PostMapping("/admin/clients/{id}/delete")
    public String delete(@PathVariable String id) {
        clientService.delete(id);
        return "redirect:/admin/clients";
    }

    @PostMapping("/admin/clients/{id}/password")
    public String changePassword(@PathVariable String id, @Valid @ModelAttribute("passwordForm") PasswordForm form,
                                 BindingResult bindingResult, Model model) {
        if (!bindingResult.hasErrors()) {
            try {
                clientService.changePassword(id, form.password(), form.confirmation());
                return "redirect:/admin/clients/" + id;
            } catch (IllegalArgumentException exception) {
                model.addAttribute("passwordError", exception.getMessage());
            }
        }
        model.addAttribute("client", clientService.findByCode(id));
        return "admin/clients/details";
    }

    @GetMapping("/admin/clients/{id}/edit")
    public String edit(@PathVariable String id, Model model) {
        var client = clientService.findByCode(id);
        model.addAttribute("clientCode", id);
        model.addAttribute("clientForm", new EditForm(client.getName(), client.getGender(), client.getBirth(),
                client.getCpf(), client.getEmail(), client.getPhoneType(), client.getDdd(), client.getPhoneNumber()));
        return "admin/clients/edit";
    }

    @PostMapping("/admin/clients/{id}/edit")
    public String update(@PathVariable String id, @Valid @ModelAttribute("clientForm") EditForm form,
                         BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("clientCode", id);
            return "admin/clients/edit";
        }
        try {
            clientService.update(id, form.name(), form.gender(), form.birth(), form.cpf(), form.email(),
                    form.phoneType(), form.ddd(), form.phoneNumber());
        } catch (IllegalArgumentException exception) {
            model.addAttribute("formError", exception.getMessage());
            return "admin/clients/edit";
        }
        return "redirect:/admin/clients/" + id;
    }

    public record ClientForm(
            @NotBlank String name,
            @NotBlank String gender,
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birth,
            @NotBlank String cpf,
            @NotBlank @Email String email,
            @NotBlank String phoneType,
            @NotBlank String ddd,
            @NotBlank String phoneNumber,
            @NotBlank String password,
            @NotBlank String passwordConfirmation,
            @Valid AddressForm deliveryAddress,
            @Valid AddressForm billingAddress,
            boolean sameAddress) {
        public record AddressForm(
                String label,
                String residenceType,
                String streetType,
                String street,
                String number,
                String neighborhood,
                String cep,
                String city,
                String state,
                String notes) {
            private ClientService.AddressData toData() {
                return new ClientService.AddressData(label, residenceType, streetType, street, number,
                    neighborhood, cep, city, state, notes);
            }
        }

        public ClientForm() {
            this(null, null, null, null, null, null, null, null, null, null,
                    new AddressForm(null, null, null, null, null, null, null, null, null, null),
                    new AddressForm(null, null, null, null, null, null, null, null, null, null), false);
        }
    }

    public record EditForm(@NotBlank String name, @NotBlank String gender,
                           @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birth,
                           @NotBlank String cpf, @NotBlank @Email String email, @NotBlank String phoneType,
                           @NotBlank String ddd, @NotBlank String phoneNumber) {}

    public record PasswordForm(@NotBlank String password, @NotBlank String confirmation) {}
}
