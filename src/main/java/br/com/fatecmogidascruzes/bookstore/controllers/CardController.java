package br.com.fatecmogidascruzes.bookstore.controllers;

import br.com.fatecmogidascruzes.bookstore.entities.ClientCard;
import br.com.fatecmogidascruzes.bookstore.services.CardService;
import br.com.fatecmogidascruzes.bookstore.services.CardService.CardData;
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
public class CardController {
    private final CardService cardService;

    public CardController(CardService cardService) { this.cardService = cardService; }

    @GetMapping("/admin/clients/{clientCode}/cards")
    public String index(@PathVariable String clientCode, Model model) {
        model.addAttribute("clientCode", clientCode);
        model.addAttribute("cards", cardService.findByClient(clientCode));
        return "admin/clients/cards/index";
    }

    @GetMapping("/admin/clients/{clientCode}/cards/new")
    public String create(@PathVariable String clientCode, Model model) {
        cardService.findByClient(clientCode);
        model.addAttribute("clientCode", clientCode);
        model.addAttribute("brands", cardService.findBrands());
        model.addAttribute("cardForm", new CardForm());
        return "admin/clients/cards/form";
    }

    @PostMapping("/admin/clients/{clientCode}/cards")
    public String store(@PathVariable String clientCode, @Valid @ModelAttribute("cardForm") CardForm form,
                        BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) return formView(clientCode, form, model);
        try {
            cardService.create(clientCode, form.toData());
            return "redirect:/admin/clients/" + clientCode + "/cards";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("formError", exception.getMessage());
            return formView(clientCode, form, model);
        }
    }

    @GetMapping("/admin/clients/{clientCode}/cards/{id}/edit")
    public String edit(@PathVariable String clientCode, @PathVariable Long id, Model model) {
        model.addAttribute("clientCode", clientCode);
        model.addAttribute("cardId", id);
        model.addAttribute("brands", cardService.findBrands());
        model.addAttribute("cardForm", CardForm.from(cardService.find(clientCode, id)));
        return "admin/clients/cards/form";
    }

    @PostMapping("/admin/clients/{clientCode}/cards/{id}")
    public String update(@PathVariable String clientCode, @PathVariable Long id, @Valid @ModelAttribute("cardForm") CardForm form,
                         BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) return formView(clientCode, form, model, id);
        try {
            cardService.update(clientCode, id, form.toData());
            return "redirect:/admin/clients/" + clientCode + "/cards";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("formError", exception.getMessage());
            return formView(clientCode, form, model, id);
        }
    }

    @PostMapping("/admin/clients/{clientCode}/cards/{id}/preferred")
    public String preferred(@PathVariable String clientCode, @PathVariable Long id) {
        cardService.setPreferred(clientCode, id);
        return "redirect:/admin/clients/" + clientCode + "/cards";
    }

    @PostMapping("/admin/clients/{clientCode}/cards/{id}/delete")
    public String delete(@PathVariable String clientCode, @PathVariable Long id) {
        cardService.delete(clientCode, id);
        return "redirect:/admin/clients/" + clientCode + "/cards";
    }

    private String formView(String clientCode, CardForm form, Model model) { return formView(clientCode, form, model, null); }
    private String formView(String clientCode, CardForm form, Model model, Long id) {
        model.addAttribute("clientCode", clientCode);
        model.addAttribute("cardId", id);
        model.addAttribute("brands", cardService.findBrands());
        return "admin/clients/cards/form";
    }

    public record CardForm(String number, @NotBlank String holderName, @NotBlank String brand,
                           String securityCode, boolean preferred) {
        public CardForm() { this(null, null, null, null, false); }
        private CardData toData() { return new CardData(number, holderName, brand, securityCode, preferred); }
        private static CardForm from(ClientCard card) {
            return new CardForm("", card.getHolderName(), card.getBrand().getName(), "", card.isPreferred());
        }
    }
}
