package eu.arrowhead.core.mscv;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import eu.arrowhead.common.database.entity.mscv.Mip;
import eu.arrowhead.common.database.entity.mscv.SshTarget;
import eu.arrowhead.common.database.entity.mscv.VerificationEntry;
import eu.arrowhead.common.database.entity.mscv.VerificationEntryList;
import eu.arrowhead.common.database.entity.mscv.VerificationExecution;
import eu.arrowhead.common.database.view.mscv.MipView;
import eu.arrowhead.common.database.view.mscv.MipViewImpl;
import eu.arrowhead.common.database.view.mscv.VerificationEntryViewImpl;
import eu.arrowhead.common.database.view.mscv.VerificationExecutionView;
import eu.arrowhead.common.database.view.mscv.VerificationExecutionViewImpl;
import eu.arrowhead.common.database.view.mscv.VerificationListView;
import eu.arrowhead.common.database.view.mscv.VerificationListViewImpl;
import eu.arrowhead.common.dto.shared.mscv.SshTargetDto;

public class MscvDtoConverter {
    private MscvDtoConverter() { super(); }

    public static Set<VerificationEntryViewImpl> convert(final Collection<VerificationEntry> entries) {
        final var resultSet = new HashSet<VerificationEntryViewImpl>();
        for (VerificationEntry entry : entries) {
            resultSet.add(convert(entry));
        }
        return resultSet;
    }

    public static VerificationEntryViewImpl convert(final VerificationEntry entry) {
        if (Objects.isNull(entry)) { return null; }
        return new VerificationEntryViewImpl(entry);
    }

    public static MipView convert(final Mip mip) {
        if (Objects.isNull(mip)) { return null; }
        return new MipViewImpl(mip);
    }

    public static VerificationListView convert(final VerificationEntryList entryList) {
        return new VerificationListViewImpl(entryList);
    }

    public static VerificationExecutionView convert(final VerificationExecution execution) {
        return new VerificationExecutionViewImpl(execution);
    }

    public static SshTargetDto convert(SshTarget sshTarget) {
        if (sshTarget == null) {
            return null;
        }
        SshTargetDto sshTargetDto = new SshTargetDto();
        sshTargetDto.setAddress(sshTarget.getAddress());
        sshTargetDto.setPort(sshTarget.getPort());
        sshTargetDto.setName(sshTarget.getName());
        sshTargetDto.setOs(sshTarget.getOs());

        return sshTargetDto;
    }

    public static SshTarget convert(SshTargetDto sshTargetDto) {
        if (sshTargetDto == null) {
            return null;
        }
        SshTarget sshTarget = new SshTarget();
        sshTarget.setAddress(sshTargetDto.getAddress());
        sshTarget.setPort(sshTargetDto.getPort());
        sshTarget.setName(sshTargetDto.getName());
        sshTarget.setOs(sshTargetDto.getOs());
        return sshTarget;
    }
}
