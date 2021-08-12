package eu.arrowhead.core.mscv;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import eu.arrowhead.common.database.entity.mscv.Mip;
import eu.arrowhead.common.database.entity.mscv.MipCategory;
import eu.arrowhead.common.database.entity.mscv.MipDomain;
import eu.arrowhead.common.database.entity.mscv.Script;
import eu.arrowhead.common.database.entity.mscv.SshTarget;
import eu.arrowhead.common.database.entity.mscv.Standard;
import eu.arrowhead.common.database.entity.mscv.Target;
import eu.arrowhead.common.database.entity.mscv.VerificationEntry;
import eu.arrowhead.common.database.entity.mscv.VerificationEntryList;
import eu.arrowhead.common.database.entity.mscv.VerificationResult;
import eu.arrowhead.common.database.view.mscv.MipView;
import eu.arrowhead.common.database.view.mscv.MipViewImpl;
import eu.arrowhead.common.database.view.mscv.VerificationEntryViewImpl;
import eu.arrowhead.common.database.view.mscv.VerificationExecutionView;
import eu.arrowhead.common.database.view.mscv.VerificationExecutionViewImpl;
import eu.arrowhead.common.database.view.mscv.VerificationListView;
import eu.arrowhead.common.database.view.mscv.VerificationListViewImpl;
import eu.arrowhead.common.dto.shared.mscv.CategoryDto;
import eu.arrowhead.common.dto.shared.mscv.DomainDto;
import eu.arrowhead.common.dto.shared.mscv.MipDto;
import eu.arrowhead.common.dto.shared.mscv.MipIdentifierDto;
import eu.arrowhead.common.dto.shared.mscv.ScriptRequestDto;
import eu.arrowhead.common.dto.shared.mscv.ScriptResponseDto;
import eu.arrowhead.common.dto.shared.mscv.SshTargetDto;
import eu.arrowhead.common.dto.shared.mscv.StandardDto;
import eu.arrowhead.common.dto.shared.mscv.TargetDto;

public class MscvDtoConverter {
    private MscvDtoConverter() { super(); }

    public static Set<VerificationEntryViewImpl> convertToView(final Collection<VerificationEntry> entries) {
        final HashSet<VerificationEntryViewImpl> resultSet = new HashSet<>();
        for (VerificationEntry entry : entries) {
            resultSet.add(convertToView(entry));
        }
        return resultSet;
    }

    public static VerificationEntryViewImpl convertToView(final VerificationEntry entry) {
        return Objects.nonNull(entry) ? new VerificationEntryViewImpl(entry) : null;
    }

    public static MipView convertToView(final Mip mip) {
        return Objects.nonNull(mip) ? new MipViewImpl(mip) : null;
    }

    public static VerificationListView convertToView(final VerificationEntryList entryList) {
        return new VerificationListViewImpl(entryList);
    }

    public static VerificationExecutionView convertToView(final VerificationResult execution) {
        return new VerificationExecutionViewImpl(execution);
    }

    public static SshTargetDto convert(final SshTarget sshTarget) {
        if (Objects.isNull(sshTarget)) {
            return null;
        }

        SshTargetDto sshTargetDto = new SshTargetDto();
        sshTargetDto.setAddress(safeTrim(sshTarget.getAddress()));
        sshTargetDto.setPort(sshTarget.getPort());
        sshTargetDto.setName(safeTrim(sshTarget.getName()));
        sshTargetDto.setOs(sshTarget.getOs());
        return sshTargetDto;
    }

    public static SshTarget convert(final SshTargetDto sshTargetDto) {
        if (Objects.isNull(sshTargetDto)) {
            return null;
        }

        SshTarget sshTarget = new SshTarget();
        sshTarget.setAddress(safeTrim(sshTargetDto.getAddress()));
        sshTarget.setPort(sshTargetDto.getPort());
        sshTarget.setName(safeTrim(sshTargetDto.getName()));
        sshTarget.setOs(sshTargetDto.getOs());
        return sshTarget;
    }

    public static Target convert(final TargetDto targetDto) {
        if (Objects.isNull(targetDto)) {
            return null;
        } else if (SshTargetDto.class.isAssignableFrom(targetDto.getClass())) {
            return convert((SshTargetDto) targetDto);
        } else {
            throw new UnsupportedOperationException("Conversion of target not supported!");
        }
    }

    public static CategoryDto convert(final MipCategory mipCategory) {
        return Objects.nonNull(mipCategory) ? new CategoryDto(safeTrim(mipCategory.getName()), safeTrim(mipCategory.getAbbreviation())) : null;
    }

    public static MipCategory convert(final CategoryDto dto) {
        return Objects.nonNull(dto) ? new MipCategory(safeTrim(dto.getName()), safeTrim(dto.getAbbreviation())) : null;
    }

    @SuppressWarnings("DuplicatedCode")
    public static MipDto convert(final Mip mip) {
        if (Objects.isNull(mip)) {
            return null;
        }
        final MipDto dto = new MipDto();
        dto.setName(safeTrim(mip.getName()));
        dto.setDescription(safeTrim(mip.getDescription()));
        dto.setCategory(convert(mip.getCategory()));
        dto.setDomain(convert(mip.getDomain()));
        dto.setStandard(convert(mip.getStandard()));
        dto.setExtId(mip.getExtId());
        return dto;
    }

    @SuppressWarnings("DuplicatedCode")
    public static Mip convert(final MipDto dto) {
        if (Objects.isNull(dto)) {
            return null;
        }
        final Mip mip = new Mip();
        mip.setName(safeTrim(dto.getName()));
        mip.setDescription(safeTrim(dto.getDescription()));
        mip.setCategory(convert(dto.getCategory()));
        mip.setDomain(convert(dto.getDomain()));
        mip.setStandard(convert(dto.getStandard()));
        mip.setExtId(dto.getExtId());
        return mip;
    }

    private static MipDomain convert(final DomainDto domain) {
        return Objects.nonNull(domain) ? new MipDomain(safeTrim(domain.getName())) : null;
    }

    private static DomainDto convert(final MipDomain domain) {
        return Objects.nonNull(domain) ? new DomainDto(safeTrim(domain.getName())) : null;
    }

    public static Script convert(final ScriptRequestDto dto) {
        if (dto == null) {
            return null;
        }
        Script script = new Script();
        script.setMip(convert(dto.getMip()));
        script.setLayer(dto.getLayer());
        script.setOs(dto.getOs());
        return script;
    }

    private static Mip convert(final MipIdentifierDto dto) {
        if (dto == null) {
            return null;
        }
        final Mip mip = new Mip();
        final MipCategory category = new MipCategory();
        category.setAbbreviation(safeTrim(dto.getCategoryAbbreviation()));
        mip.setCategory(category);
        mip.setExtId(dto.getExtId());
        return mip;
    }

    public static ScriptResponseDto convert(final Script dto) {
        if (dto == null) {
            return null;
        }
        ScriptResponseDto script = new ScriptResponseDto();
        script.setMip(convert(dto.getMip()));
        script.setLayer(dto.getLayer());
        script.setOs(dto.getOs());
        return script;
    }

    public static Standard convert(final StandardDto dto) {
        if (dto == null) {
            return null;
        }
        Standard standard = new Standard();
        standard.setIdentification(safeTrim(dto.getIdentification()));
        standard.setName(safeTrim(dto.getName()));
        standard.setReferenceUri(safeTrim(dto.getReferenceUri()));
        standard.setDescription(safeTrim(dto.getDescription()));
        return standard;
    }
    public static StandardDto convert(final Standard standard) {
        if (standard == null) {
            return null;
        }
        StandardDto dto = new StandardDto();
        dto.setIdentification(safeTrim(standard.getIdentification()));
        dto.setName(safeTrim(standard.getName()));
        dto.setReferenceUri(safeTrim(standard.getReferenceUri()));
        dto.setDescription(safeTrim(standard.getDescription()));
        return dto;
    }

    public static TargetDto convert(final Target target) {
        if (target == null) {
            return null;
        }
        return new TargetDto(safeTrim(target.getName()), target.getOs());
    }

    private static String safeTrim(final String string) {
        return Objects.nonNull(string) ? string.trim() : null;
    }
}
