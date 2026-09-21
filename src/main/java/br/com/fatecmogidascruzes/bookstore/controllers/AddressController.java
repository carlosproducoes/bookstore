package br.com.fatecmogidascruzes.bookstore.controllers;

import br.com.fatecmogidascruzes.bookstore.entities.ClientAddress;
import br.com.fatecmogidascruzes.bookstore.services.AddressService;
import br.com.fatecmogidascruzes.bookstore.services.AddressService.AddressData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AddressController {
    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/admin/clients/{clientCode}/addresses")
    public String index(@PathVariable String clientCode, Model model) {
        model.addAttribute("clientCode", clientCode);
        model.addAttribute("addresses", addressService.findByClient(clientCode));
        return "admin/clients/addresses/index";
    }

    @GetMapping("/admin/clients/{clientCode}/addresses/new")
    public String create(@PathVariable String clientCode, Model model) {
        addressService.findByClient(clientCode);
        model.addAttribute("clientCode", clientCode);
        model.addAttribute("addressForm", new AddressForm());
        return "admin/clients/addresses/form";
    }

    @PostMapping("/admin/clients/{clientCode}/addresses")
    public String store(@PathVariable String clientCode, @Valid @ModelAttribute("addressForm") AddressForm form,
                        BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return formView(clientCode, form, model);
        }
        try {
            addressService.create(clientCode, form.toData());
            return "redirect:/admin/clients/" + clientCode + "/addresses";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("formError", exception.getMessage());
            return formView(clientCode, form, model);
        }
    }

    @GetMapping("/admin/clients/{clientCode}/addresses/{id}/edit")
    public String edit(@PathVariable String clientCode, @PathVariable Long id, Model model) {
        model.addAttribute("clientCode", clientCode);
        model.addAttribute("addressId", id);
        model.addAttribute("addressForm", AddressForm.from(addressService.find(clientCode, id)));
        return "admin/clients/addresses/form";
    }

    @PostMapping("/admin/clients/{clientCode}/addresses/{id}")
    public String update(@PathVariable String clientCode, @PathVariable Long id,
                         @Valid @ModelAttribute("addressForm") AddressForm form,
                         BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("clientCode", clientCode);
            model.addAttribute("addressId", id);
            return formView(clientCode, form, model);
        }
        try {
            addressService.update(clientCode, id, form.toData());
            return "redirect:/admin/clients/" + clientCode + "/addresses";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("formError", exception.getMessage());
            model.addAttribute("clientCode", clientCode);
            model.addAttribute("addressId", id);
            return formView(clientCode, form, model);
        }
    }

    @PostMapping("/admin/clients/{clientCode}/addresses/{id}/delete")
    public String delete(@PathVariable String clientCode, @PathVariable Long id, Model model) {
        try {
            addressService.delete(clientCode, id);
            return "redirect:/admin/clients/" + clientCode + "/addresses";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("clientCode", clientCode);
            model.addAttribute("addresses", addressService.findByClient(clientCode));
            model.addAttribute("formError", exception.getMessage());
            return "admin/clients/addresses/index";
        }
    }

    private String formView(String clientCode, AddressForm form, Model model) {
        model.addAttribute("clientCode", clientCode);
        return "admin/clients/addresses/form";
    }

    public record AddressForm(
            @NotBlank String label,
            @NotBlank String type,
            @NotBlank String residenceType,
            @NotBlank String streetType,
            @NotBlank String street,
            @NotBlank String number,
            @NotBlank String neighborhood,
            @NotBlank String cep,
            @NotBlank String city,
            @NotBlank String state,
            String notes) {
        public AddressForm() {
            this(null, null, null, null, null, null, null, null, null, null, null);
        }

        private AddressData toData() {
            return new AddressData(label, type, residenceType, streetType, street, number,
                    neighborhood, cep, city, state, notes);
        }

        private static AddressForm from(ClientAddress address) {
            return new AddressForm(address.getLabel(), address.getType(), address.getResidenceType(),
                    address.getStreetType(), address.getStreet(), address.getNumber(), address.getNeighborhood(),
                    address.getCep(), address.getCity(), address.getState(), address.getNotes());
        }
    }
}
