package com.big.dreamer.doccentral.document.carsale.template;

import com.big.dreamer.doccentral.document.template.EditableTemplateRepository;
import com.big.dreamer.doccentral.storage.ApplicationDirectories;
import org.springframework.stereotype.Component;

@Component
public class CarSaleTemplateRepository extends EditableTemplateRepository {
    public CarSaleTemplateRepository(ApplicationDirectories directories) {
        super(directories.templatesDirectory("car-sale"), "car-sale");
    }
}
