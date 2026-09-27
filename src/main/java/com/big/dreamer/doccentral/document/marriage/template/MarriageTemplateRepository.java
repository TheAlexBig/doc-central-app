package com.big.dreamer.doccentral.document.marriage.template;

import com.big.dreamer.doccentral.document.template.EditableTemplateRepository;
import com.big.dreamer.doccentral.storage.ApplicationDirectories;
import org.springframework.stereotype.Component;

@Component
public class MarriageTemplateRepository extends EditableTemplateRepository {
    public MarriageTemplateRepository(ApplicationDirectories directories) {
        super(directories.templatesDirectory("marriage"), "marriage");
    }
}
