package com.big.dreamer.doccentral.document.mutual.template;

import com.big.dreamer.doccentral.document.template.EditableTemplateRepository;
import com.big.dreamer.doccentral.storage.ApplicationDirectories;
import org.springframework.stereotype.Component;

@Component
public class MutualTemplateRepository extends EditableTemplateRepository {
    public MutualTemplateRepository(ApplicationDirectories directories) {
        super(directories.templatesDirectory("mutual"), "mutual");
    }
}
