package awadh.bakery.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import awadh.bakery.models.Customer;
import awadh.bakery.models.CustomerRepository;


@Controller
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;
    @Value("${upload.directory}") // Get the upload directory path from application.properties
    private String uploadDirectory;
    
    
    @GetMapping("/")
    public String redirectToCustomerList() {
        return "redirect:/customers/list";
    }
    
    @GetMapping("/customers/list")
    public String ListCustomer(Model model) {
    	List<Customer> customers=customerRepository.findAll();
    	model.addAttribute("customers",customers);
    	return "customer-list";
    }

    @GetMapping("/customer/form")
    public String showCustomerForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customer-form";
    }

    @PostMapping("/saveCustomer")
    public String saveCustomer(@ModelAttribute("customer") Customer customer,BindingResult bindingResult,
            @RequestParam("imageFile") MultipartFile imageFile, RedirectAttributes redirectAttributes)throws IOException {
    		
    	if (bindingResult.hasErrors()) {
            // Validation errors occurred, return to the form page
            return "customer-form";
        }

        // Validate the image size here
        if (!imageFile.isEmpty() && imageFile.getSize() > 300 * 1024) {
            bindingResult.rejectValue("imageFile", "image.size", "Image size exceeds the allowed limit");
            return "customer-form";
        }

        // Save the image to the server
        if (!imageFile.isEmpty()) {
            String uploadDirectory = "src/main/resources/customerImages"; // Specify the storage location
            String originalFileName = imageFile.getOriginalFilename();
            String uniqueFileName = System.currentTimeMillis() + "_" + originalFileName;

            try {
                File file = new File(uploadDirectory, uniqueFileName);
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(imageFile.getBytes());
                fileOutputStream.close();
            } catch (IOException e) {
                // Handle any IO exception
                e.printStackTrace();
                // You can return an error message or perform other error handling here
            }
            customer.setImageFileName(uniqueFileName);
        }

        // Save the customer data to the database
        customerRepository.save(customer);

        // Redirect to the customer list page
        redirectAttributes.addFlashAttribute("successMessage", "Customer saved successfully");
        return "redirect:/customers/list";

    }
    
    @GetMapping("/customer/edit/{id}")
    public String showEditCustomerForm(@PathVariable("id") Long id, Model model) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid customer ID: " + id));
        model.addAttribute("customer", customer);
        return "update-form";
    }
    
    @PostMapping("/customer/update")
    public String UpdateCustomer(@ModelAttribute("customer") Customer customer) {
    	customerRepository.save(customer);
    	return "redirect:/customers/list";
    }

    @GetMapping("/customer/delete/{id}")
    public String deleteCustomer(@PathVariable("id") Long id) {
        customerRepository.deleteById(id);
        return "redirect:/customers/list";
    }

}
