package revolut.backendtest.api.spark;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.util.function.Supplier;
import revolut.backendtest.api.BadRequest;
import revolut.backendtest.api.NotFound;
import revolut.backendtest.api.dto.MakeTransferRequest;
import revolut.backendtest.model.AccountId;
import revolut.backendtest.model.Transfer;
import revolut.backendtest.model.TransferId;
import revolut.backendtest.service.AccountNotFoundException;
import revolut.backendtest.service.IllegalAmountException;
import revolut.backendtest.service.NotEnoughMoneyException;
import revolut.backendtest.service.SelfTransferException;
import revolut.backendtest.service.TransferService;
import spark.Request;
import spark.Response;

class TransferController {

  private final RequestTransformer transformer;
  private final TransferService transferService;

  @Inject
  TransferController(
      RequestTransformer transformer,
      TransferService transferService
  ) {
    this.transformer = transformer;
    this.transferService = transferService;
  }

  private static TransferId getTransferId(Request request) {
    return parseTransferId(request.params(":id"));
  }

  private static TransferId parseTransferId(String id) {
    try {
      return new TransferId(Long.parseLong(id));
    } catch (NumberFormatException ex) {
      throw new BadRequest("Incorrect transfer id: " + id, ex);
    }
  }

  private static Supplier<NotFound> notFound(TransferId transferId) {
    return () -> new NotFound("Cannot find transfer with id=" + transferId.value);
  }

  ImmutableList<Transfer> getTransfers(Request request, Response response) {
    return transferService.getAll();
  }

  Transfer getTransfer(Request request, Response response) {
    TransferId transferId = getTransferId(request);
    return transferService.get(transferId).orElseThrow(notFound(transferId));
  }

  Transfer makeTransfer(Request request, Response response) {
    MakeTransferRequest rq = transformer.transform(request, MakeTransferRequest.class);
    if (rq.amount == null) {
      throw new BadRequest("Missing transfer amount");
    }
    if (rq.from == null || rq.to == null) {
      throw new BadRequest("from/to is missing");
    }

    try {
      return tryMakeTransfer(rq);
    } catch (AccountNotFoundException ex) {
      throw new NotFound(ex.getMessage(), ex);
    } catch (NotEnoughMoneyException | IllegalAmountException | SelfTransferException ex) {
      throw new BadRequest(ex.getMessage(), ex);
    }
  }

  private Transfer tryMakeTransfer(MakeTransferRequest rq)
      throws AccountNotFoundException, NotEnoughMoneyException, IllegalAmountException, SelfTransferException {
    return transferService.makeTransfer(
        new AccountId(rq.from),
        new AccountId(rq.to),
        rq.amount
    );
  }
}
