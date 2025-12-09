package com.rental.blockchain.contract;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.StaticStruct;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple12;
import org.web3j.tuples.generated.Tuple4;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.9.8.
 */
@SuppressWarnings("rawtypes")
public class RentalPlatform extends Contract {
    public static final String BINARY = "0x60803461011e57601f611dde38819003918201601f19168301916001600160401b038311848410176101235780849260209460405283398101031261011e57516001600160a01b038082169182900361011e57600160005533156101055760018054336001600160a01b0319808316821790935560405192939091167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e0600080a36005600b5582156100c35750600a541617600a55604051611ca4908161013a8239f35b62461bcd60e51b815260206004820152601760248201527f416472657373652077616c6c657420696e76616c6964650000000000000000006044820152606490fd5b604051631e4fbdf760e01b815260006004820152602490fd5b600080fd5b634e487b7160e01b600052604160045260246000fdfe6080806040526004361015610097575b5036156100525760405162461bcd60e51b8152602060048201526014602482015273466f6e6374696f6e20696e6578697374616e746560601b6044820152606490fd5b60405162461bcd60e51b815260206004820152601d60248201527f456e766f692064697265637420455448206e6f6e206175746f726973650000006044820152606490fd5b60003560e01c908163097b6be31461175e575080630c975765146117325780630d7556ae146116e85780630dca825e146114e757806312e8e2c314611455578063173f9bf3146113ed57806317b7e03e146113c75780631bd2eeee146113005780631d5caf27146112c15780631dab301e14611207578063267409451461117f5780633ccfd60b146110ba5780634d68282f14610f5a5780635813a68314610ed95780636a5c841a14610d39578063715018a614610cdc5780637ee8b2f8146102325780637f2af0831461083b5780638831e9cf14610c675780638da5cb5b14610c3e578063b52536ae14610c12578063b5f1626a14610aed578063cdd78cfc14610acf578063cf270c891461088d578063d287ac421461086f578063e34fc5311461083b578063e834016e1461080d578063e95a644f146106fc578063edded820146102f5578063f2fde38b1461026c578063f3f43703146102325763fa2af9da14610204573861000f565b3461022d57600036600319011261022d57600a546040516001600160a01b039091168152602090f35b600080fd5b3461022d57602036600319011261022d576001600160a01b036102536117ff565b16600052600c6020526020604060002054604051908152f35b3461022d57602036600319011261022d576102856117ff565b61028d611882565b6001600160a01b039081169081156102dc57600154826001600160601b0360a01b821617600155167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e0600080a3005b604051631e4fbdf760e01b815260006004820152602490fd5b608036600319011261022d5760243561030c61199a565b428111156106b7578060443511156106725760649081351561062357610337604435826004356119bd565b156105d357600b54808335029083358204036105bd578290049061035c8284356118ae565b918234036105805761036f6009546118bb565b908160095560405192610381846118ca565b8284526020840191338352604085019260043584526060860192835260808601604435815260a087018935815260c0880184815260e08901918a83526101008a0193600185526101208b01974289526101408c019960008b526101608d019b60008d52600052600260205260406000209c518d5560018d0160018060a01b03809951166001600160601b0360a01b8254161790555160028d01555160038c01555160048b01555160058a01555160068901555160078801555195600687101561056a57600b600097968897968897968897600886019060ff8019835416911617905551600985015551600a84015551910155338352600460205261048b6040842060095490611925565b600435835260056020526104a56040842060095490611925565b600a54165af16104b361195a565b5015610527576020906009549060405190600435825283820152817f06305cc4b9ada869f34253a9663739915d901b5b1fd528cb0ccf4c3327185fea60403393a360405190807f853fc6307cc2d72d0ec45ec77f198703d63dd760dedc66cf5488bc9727d69f9c600080a260016000558152f35b5060405162461bcd60e51b815260206004820181905260248201527f4563686563207472616e736665727420667261697320706c617465666f726d656044820152fd5b634e487b7160e01b600052602160045260246000fd5b60405162461bcd60e51b8152602060048201526016602482015275135bdb9d185b9d081c185e59481a5b98dbdc9c9958dd60521b60448201528490fd5b634e487b7160e01b600052601160045260246000fd5b60405162461bcd60e51b8152602060048201526024808201527f4c6f67656d656e742064656a61207265736572766520706f75722063657320646044820152636174657360e01b81840152608490fd5b60405162461bcd60e51b815260206004820152602260248201527f4d6f6e74616e74206c6f636174696f6e20646f6974206574726520706f73697460448201526134b360f11b81840152608490fd5b60405162461bcd60e51b815260206004820152601960248201527f446174652066696e2061707265732064617465206465627574000000000000006044820152606490fd5b60405162461bcd60e51b815260206004820152601b60248201527f4461746520646562757420646f697420657472652066757475726500000000006044820152606490fd5b3461022d5760208060031936011261022d5760043590816000526002815261073560018060a01b03600160406000200154163314611ac2565b816000526002815261074d6040600020541515611b20565b816000526002815260ff60086040600020015416600681101561056a5760016107769114611b6c565b81600052600281526040600020600381015442106107cf57908160087f9c99fe7131fc7f333f6914ed4df9e39bd698eae4bb00c0dd5583ed4281a36e3e9301600260ff19825416179055600a42910155604051428152a2005b60405162461bcd60e51b81526004810183905260166024820152752a3937b8103a37ba103837bab91031b432b1b596b4b760511b6044820152606490fd5b3461022d57606036600319011261022d5760206108316044356024356004356119bd565b6040519015158152f35b3461022d57602036600319011261022d576004356000526006602052602060018060a01b0360406000205416604051908152f35b3461022d57600036600319011261022d576020600954604051908152f35b3461022d57604036600319011261022d5760043560249081359060ff821680920361022d57806000526002906020918083526108cf6040600020541515611b20565b8160005280835260018060a01b03916108f383600160406000200154163314611ac2565b8060005281845260ff600860406000200154166006811015610aba57600461091b9114611b6c565b600185101580610aaf575b15610a6b5760005280835280604060002001918254918260005260038552604060002090604051610956816118e7565b338152868101948552604081019188835260608201934285528054600160401b811015610a565761098c91600182018155611859565b969096610a415760039560ff9351166001600160601b0360a01b885416178755516001870155850191511660ff198254161790555191015580546000526007825260406000206109dd8482546118ae565b9055805460005260088252604060002080549060018201809211610a2c575554604051928352339290917fd4f4c9f4676cc874235be122f0fff5ccfad569cdb7c635c21c8b4adfe678a3d691a3005b85634e487b7160e01b60005260116004526000fd5b8a634e487b7160e01b60005260006004526000fd5b8b634e487b7160e01b60005260416004526000fd5b60405162461bcd60e51b815260048101859052601b818801527f4e6f746520646f6974206574726520656e7472652031206574203500000000006044820152606490fd5b506005851115610926565b86634e487b7160e01b60005260216004526000fd5b3461022d57600036600319011261022d576020600b54604051908152f35b3461022d5760208060031936011261022d57600435600052600390818152604060002091825467ffffffffffffffff8111610bfc5760405191610b35848360051b0184611903565b81835260009485528385208484019591865b848410610bb357604080518881528751818a018190528a928201908a60005b828110610b735784840385f35b855180516001600160a01b03168552808301518584015260408082015160ff16908601526060908101519085015294810194608090930192600101610b66565b600487600192604051610bc5816118e7565b848060a01b038754168152848701548382015260ff6002880154166040820152858701546060820152815201930193019291610b47565b634e487b7160e01b600052604160045260246000fd5b3461022d57602036600319011261022d5760043560005260076020526020604060002054604051908152f35b3461022d57600036600319011261022d576001546040516001600160a01b039091168152602090f35b3461022d57602036600319011261022d57610c806117ff565b610c88611882565b6001600160a01b0390811690610c9f821515611c2f565b600a54826001600160601b0360a01b821617600a55167f5812a713ab7654faa23157e4c40ea2b3560cfea6fd6c09a2e0adf6cbe460e1c0600080a3005b3461022d57600036600319011261022d57610cf5611882565b600180546001600160a01b031981169091556000906001600160a01b03167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e08280a3005b3461022d57602036600319011261022d576000610160604051610d5b816118ca565b8281528260208201528260408201528260608201528260808201528260a08201528260c08201528260e0820152826101008201528261012082015282610140820152015260043560005260026020526040600020604051610dbb816118ca565b8154815260018201546001600160a01b03166020820152600282015460408201526003820154606082015260048201546080820152600582015460a082015260068083015460c0830152600783015460e0830152600883015460ff169081101561056a5761018092600b916101008401526009810154610120840152600a8101546101408401520154610160820152610160604051918051835260018060a01b03602082015116602084015260408101516040840152606081015160608401526080810151608084015260a081015160a084015260c081015160c084015260e081015160e0840152610eb7610100820151610100850190611875565b6101208101516101208401526101408101516101408401520151610160820152f35b3461022d57604036600319011261022d576024356001600160a01b038116906004359082900361022d57610f0b611882565b610f16821515611c2f565b600081815260066020526040812080546001600160a01b031916841790557f2512e4083f183f1ec4e39fd334b307ca312d878658c6b24bd23e59cbe930f3729080a3005b3461022d5760208060031936011261022d5760043590610f78611882565b610f8061199a565b8160005260028152610f986040600020541515611b20565b816000526002815260ff60086040600020015416600681101561056a576003610fc19114611b6c565b60008281526002808352604080832091820154835260068452909120546001600160a01b03169190821561107557839160058260087f21d71db5be59bb9fa133895586b7404307dd33fb93b16db09dc6f1d9d7d231b09401600460ff1982541617905501805485600052600c835261103f60406000209182546118ae565b905554604051908152a37fe70f5cb5ecde9644c195a97cf45a506de023d1d170cd972f04441fafd993f150600080a26001600055005b60405162461bcd60e51b815260048101839052601b60248201527f50726f707269657461697265206e6f6e20656e726567697374726500000000006044820152606490fd5b3461022d57600036600319011261022d576110d361199a565b33600052600c602052604060002054801561114957600080808093338252600c602052816040812055335af161110761195a565b5015611114576001600055005b60405162461bcd60e51b815260206004820152600d60248201526c1158da1958c81c995d1c985a5d609a1b6044820152606490fd5b60405162461bcd60e51b815260206004820152600e60248201526d2934b2b71030903932ba34b932b960911b6044820152606490fd5b3461022d5760208060031936011261022d576001600160a01b036111a16117ff565b1660005260048152604060002090604051908181845491828152019360005281600020916000905b8282106111f0576111ec856111e081890382611903565b604051918291826117c4565b0390f35b8354865294850194600193840193909101906111c9565b3461022d57602036600319011261022d576004356000526002602052610180604060002080549060018060a01b036001820154169060028101546112ad60038301546004840154600585015460068601549060078701549260ff60088901541694600989015497600b600a8b01549a01549a6040519c8d5260208d015260408c015260608b015260808a015260a089015260c088015260e0870152610100860190611875565b610120840152610140830152610160820152f35b3461022d576112cf36611843565b9060005260056020526040600020805482101561022d576020916112f291611815565b90546040519160031b1c8152f35b3461022d5760208060031936011261022d5760043590816000526002815261133960018060a01b03600160406000200154163314611ac2565b81600052600281526113516040600020541515611b20565b816000526002815260ff6008604060002001541690600682101561056a5761139c60027fa2e7325adf645f4435f863a2e80aca420b04de6895e94ca6192306599a7d154f9314611b6c565b8260005260028152604060002060088101600360ff19825416179055600b42910155604051428152a2005b3461022d57602036600319011261022d5760206113e5600435611bb8565b604051908152f35b3461022d576113fb36611843565b9060005260036020526040600020805482101561022d5760809161141e91611859565b5060018060a01b0381541690600181015490600360ff60028301541691015491604051938452602084015260408301526060820152f35b3461022d57602036600319011261022d57600435611471611882565b600a81116114b25760407fd347e206f25a89b917fc9482f1a2d294d749baa4dc9bde7fb495ee11fe49164391600b549080600b5582519182526020820152a1005b60405162461bcd60e51b815260206004820152600d60248201526c4672616973206d61782031302560981b6044820152606490fd5b3461022d5760208060031936011261022d576004359061150561199a565b816000526002815260018060a01b0361152981600160406000200154163314611ac2565b82600052600282526115416040600020541515611b20565b8260005260028252604060002090600882019182549260ff8416600681101561056a57600181149081156116df575b50156116a157600382015480421015611663574281039081116105bd5762015180900460078111156116435750600580830154945b60ff1916179055826115e4575b847ff3ad9f310a1371c25f06d5a530282a227e94c2cabc99fd4afee3888080ee21fa8585604051908152a26001600055005b600080848194600183950154165af16115fb61195a565b50156116085783806115b2565b60405162461bcd60e51b81526004810183905260136024820152721158da1958c81c995b589bdd5c9cd95b595b9d606a1b6044820152606490fd5b6003116116595760058083015460011c946115a5565b60056000946115a5565b60405162461bcd60e51b81526004810187905260166024820152752a3937b8103a30b932103837bab91030b7373ab632b960511b6044820152606490fd5b60405162461bcd60e51b8152600481018690526016602482015275416e6e756c6174696f6e206e6f6e207065726d69736560501b6044820152606490fd5b90501587611570565b3461022d57604036600319011261022d576117016117ff565b6001600160a01b031660009081526004602052604090208054602435919082101561022d576020916112f291611815565b3461022d57602036600319011261022d5760043560005260086020526020604060002054604051908152f35b3461022d576020908160031936011261022d576004356000526005825260406000209182548083528183019360005281600020916000905b8282106117ad576111ec856111e081890382611903565b835486529485019460019384019390910190611796565b6020908160408183019282815285518094520193019160005b8281106117eb575050505090565b8351855293810193928101926001016117dd565b600435906001600160a01b038216820361022d57565b805482101561182d5760005260206000200190600090565b634e487b7160e01b600052603260045260246000fd5b604090600319011261022d576004359060243590565b805482101561182d5760005260206000209060021b0190600090565b90600682101561056a5752565b6001546001600160a01b0316330361189657565b60405163118cdaa760e01b8152336004820152602490fd5b919082018092116105bd57565b60001981146105bd5760010190565b610180810190811067ffffffffffffffff821117610bfc57604052565b6080810190811067ffffffffffffffff821117610bfc57604052565b90601f8019910116810190811067ffffffffffffffff821117610bfc57604052565b8054600160401b811015610bfc5761194291600182018155611815565b819291549060031b91821b91600019901b1916179055565b3d15611995573d9067ffffffffffffffff8211610bfc5760405191611989601f8201601f191660200184611903565b82523d6000602084013e565b606090565b6002600054146119ab576002600055565b604051633ee5aeb560e01b8152600490fd5b9091600091825260059060209382855260408085208151808289829454938481520190895289892092895b8b828210611aac575050506119ff92500382611903565b855b8151811015611a9f578781871b8301015187526002885282872060ff6008820154166006811015611a8b57878114908115611a80575b50611a7657600481015485109081611a68575b50611a5d57611a58906118bb565b611a01565b505050505050905090565b600391500154851138611a4a565b50611a58906118bb565b600491501438611a37565b634e487b7160e01b89526021600452602489fd5b5050505050505050600190565b85548452600195860195879550930192016119e8565b15611ac957565b60405162461bcd60e51b815260206004820152602960248201527f5365756c206c65206c6f636174616972652070657574206661697265206365746044820152683a329030b1ba34b7b760b91b6064820152608490fd5b15611b2757565b60405162461bcd60e51b815260206004820152601e60248201527f4365747465207265736572766174696f6e206e276578697374652070617300006044820152606490fd5b15611b7357565b60405162461bcd60e51b815260206004820152601e60248201527f537461747574206465207265736572766174696f6e20696e76616c69646500006044820152606490fd5b6000908082526008602052604082205415611c2b576007602052604082205490606482029180830460641490151715611c1757825260086020526040822054918215611c0357500490565b634e487b7160e01b81526012600452602490fd5b634e487b7160e01b83526011600452602483fd5b5090565b15611c3657565b60405162461bcd60e51b815260206004820152601060248201526f4164726573736520696e76616c69646560801b6044820152606490fdfea264697066735822122071ac498b42e751ba837202e1b872f077e7ef6a1a8d6bf5b11b971377e773ea1264736f6c63430008140033";

    public static final String FUNC_ADDRATING = "addRating";

    public static final String FUNC_BOOKINGCOUNTER = "bookingCounter";

    public static final String FUNC_BOOKINGS = "bookings";

    public static final String FUNC_CANCELBOOKING = "cancelBooking";

    public static final String FUNC_CHECKIN = "checkIn";

    public static final String FUNC_CHECKOUT = "checkOut";

    public static final String FUNC_CREATEBOOKING = "createBooking";

    public static final String FUNC_GETAVERAGERATING = "getAverageRating";

    public static final String FUNC_GETBOOKING = "getBooking";

    public static final String FUNC_GETPENDINGWITHDRAWAL = "getPendingWithdrawal";

    public static final String FUNC_GETPROPERTYBOOKINGS = "getPropertyBookings";

    public static final String FUNC_GETPROPERTYOWNER = "getPropertyOwner";

    public static final String FUNC_GETPROPERTYRATINGS = "getPropertyRatings";

    public static final String FUNC_GETTENANTBOOKINGS = "getTenantBookings";

    public static final String FUNC_ISPROPERTYAVAILABLE = "isPropertyAvailable";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_PENDINGWITHDRAWALS = "pendingWithdrawals";

    public static final String FUNC_PLATFORMFEEPERCENTAGE = "platformFeePercentage";

    public static final String FUNC_PLATFORMWALLET = "platformWallet";

    public static final String FUNC_PROPERTYBOOKINGS = "propertyBookings";

    public static final String FUNC_PROPERTYOWNERS = "propertyOwners";

    public static final String FUNC_PROPERTYRATINGS = "propertyRatings";

    public static final String FUNC_RATINGCOUNT = "ratingCount";

    public static final String FUNC_RATINGSUM = "ratingSum";

    public static final String FUNC_RELEASEFUNDS = "releaseFunds";

    public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";

    public static final String FUNC_SETPLATFORMFEE = "setPlatformFee";

    public static final String FUNC_SETPLATFORMWALLET = "setPlatformWallet";

    public static final String FUNC_SETPROPERTYOWNER = "setPropertyOwner";

    public static final String FUNC_TENANTBOOKINGS = "tenantBookings";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final String FUNC_WITHDRAW = "withdraw";

    public static final Event BOOKINGCANCELLED_EVENT = new Event("BookingCancelled", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event BOOKINGCOMPLETED_EVENT = new Event("BookingCompleted", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}));
    ;

    public static final Event BOOKINGCONFIRMED_EVENT = new Event("BookingConfirmed", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}));
    ;

    public static final Event BOOKINGCREATED_EVENT = new Event("BookingCreated", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event CHECKINCOMPLETED_EVENT = new Event("CheckInCompleted", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event CHECKOUTCOMPLETED_EVENT = new Event("CheckOutCompleted", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event("OwnershipTransferred", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
    ;

    public static final Event PAYMENTRELEASED_EVENT = new Event("PaymentReleased", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event PLATFORMFEEUPDATED_EVENT = new Event("PlatformFeeUpdated", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event PLATFORMWALLETCHANGED_EVENT = new Event("PlatformWalletChanged", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
    ;

    public static final Event PROPERTYOWNERSET_EVENT = new Event("PropertyOwnerSet", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>(true) {}));
    ;

    public static final Event RATINGADDED_EVENT = new Event("RatingAdded", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint8>() {}));
    ;

    @Deprecated
    protected RentalPlatform(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected RentalPlatform(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected RentalPlatform(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected RentalPlatform(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<BookingCancelledEventResponse> getBookingCancelledEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(BOOKINGCANCELLED_EVENT, transactionReceipt);
        ArrayList<BookingCancelledEventResponse> responses = new ArrayList<BookingCancelledEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            BookingCancelledEventResponse typedResponse = new BookingCancelledEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.bookingId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.refundAmount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static BookingCancelledEventResponse getBookingCancelledEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(BOOKINGCANCELLED_EVENT, log);
        BookingCancelledEventResponse typedResponse = new BookingCancelledEventResponse();
        typedResponse.log = log;
        typedResponse.bookingId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.refundAmount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<BookingCancelledEventResponse> bookingCancelledEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getBookingCancelledEventFromLog(log));
    }

    public Flowable<BookingCancelledEventResponse> bookingCancelledEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(BOOKINGCANCELLED_EVENT));
        return bookingCancelledEventFlowable(filter);
    }

    public static List<BookingCompletedEventResponse> getBookingCompletedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(BOOKINGCOMPLETED_EVENT, transactionReceipt);
        ArrayList<BookingCompletedEventResponse> responses = new ArrayList<BookingCompletedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            BookingCompletedEventResponse typedResponse = new BookingCompletedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.bookingId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static BookingCompletedEventResponse getBookingCompletedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(BOOKINGCOMPLETED_EVENT, log);
        BookingCompletedEventResponse typedResponse = new BookingCompletedEventResponse();
        typedResponse.log = log;
        typedResponse.bookingId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<BookingCompletedEventResponse> bookingCompletedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getBookingCompletedEventFromLog(log));
    }

    public Flowable<BookingCompletedEventResponse> bookingCompletedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(BOOKINGCOMPLETED_EVENT));
        return bookingCompletedEventFlowable(filter);
    }

    public static List<BookingConfirmedEventResponse> getBookingConfirmedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(BOOKINGCONFIRMED_EVENT, transactionReceipt);
        ArrayList<BookingConfirmedEventResponse> responses = new ArrayList<BookingConfirmedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            BookingConfirmedEventResponse typedResponse = new BookingConfirmedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.bookingId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static BookingConfirmedEventResponse getBookingConfirmedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(BOOKINGCONFIRMED_EVENT, log);
        BookingConfirmedEventResponse typedResponse = new BookingConfirmedEventResponse();
        typedResponse.log = log;
        typedResponse.bookingId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<BookingConfirmedEventResponse> bookingConfirmedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getBookingConfirmedEventFromLog(log));
    }

    public Flowable<BookingConfirmedEventResponse> bookingConfirmedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(BOOKINGCONFIRMED_EVENT));
        return bookingConfirmedEventFlowable(filter);
    }

    public static List<BookingCreatedEventResponse> getBookingCreatedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(BOOKINGCREATED_EVENT, transactionReceipt);
        ArrayList<BookingCreatedEventResponse> responses = new ArrayList<BookingCreatedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            BookingCreatedEventResponse typedResponse = new BookingCreatedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.bookingId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.tenant = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.propertyId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.totalAmount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static BookingCreatedEventResponse getBookingCreatedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(BOOKINGCREATED_EVENT, log);
        BookingCreatedEventResponse typedResponse = new BookingCreatedEventResponse();
        typedResponse.log = log;
        typedResponse.bookingId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.tenant = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.propertyId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.totalAmount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<BookingCreatedEventResponse> bookingCreatedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getBookingCreatedEventFromLog(log));
    }

    public Flowable<BookingCreatedEventResponse> bookingCreatedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(BOOKINGCREATED_EVENT));
        return bookingCreatedEventFlowable(filter);
    }

    public static List<CheckInCompletedEventResponse> getCheckInCompletedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(CHECKINCOMPLETED_EVENT, transactionReceipt);
        ArrayList<CheckInCompletedEventResponse> responses = new ArrayList<CheckInCompletedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            CheckInCompletedEventResponse typedResponse = new CheckInCompletedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.bookingId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static CheckInCompletedEventResponse getCheckInCompletedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(CHECKINCOMPLETED_EVENT, log);
        CheckInCompletedEventResponse typedResponse = new CheckInCompletedEventResponse();
        typedResponse.log = log;
        typedResponse.bookingId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<CheckInCompletedEventResponse> checkInCompletedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getCheckInCompletedEventFromLog(log));
    }

    public Flowable<CheckInCompletedEventResponse> checkInCompletedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(CHECKINCOMPLETED_EVENT));
        return checkInCompletedEventFlowable(filter);
    }

    public static List<CheckOutCompletedEventResponse> getCheckOutCompletedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(CHECKOUTCOMPLETED_EVENT, transactionReceipt);
        ArrayList<CheckOutCompletedEventResponse> responses = new ArrayList<CheckOutCompletedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            CheckOutCompletedEventResponse typedResponse = new CheckOutCompletedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.bookingId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static CheckOutCompletedEventResponse getCheckOutCompletedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(CHECKOUTCOMPLETED_EVENT, log);
        CheckOutCompletedEventResponse typedResponse = new CheckOutCompletedEventResponse();
        typedResponse.log = log;
        typedResponse.bookingId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<CheckOutCompletedEventResponse> checkOutCompletedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getCheckOutCompletedEventFromLog(log));
    }

    public Flowable<CheckOutCompletedEventResponse> checkOutCompletedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(CHECKOUTCOMPLETED_EVENT));
        return checkOutCompletedEventFlowable(filter);
    }

    public static List<OwnershipTransferredEventResponse> getOwnershipTransferredEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, transactionReceipt);
        ArrayList<OwnershipTransferredEventResponse> responses = new ArrayList<OwnershipTransferredEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static OwnershipTransferredEventResponse getOwnershipTransferredEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, log);
        OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
        typedResponse.log = log;
        typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getOwnershipTransferredEventFromLog(log));
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OWNERSHIPTRANSFERRED_EVENT));
        return ownershipTransferredEventFlowable(filter);
    }

    public static List<PaymentReleasedEventResponse> getPaymentReleasedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(PAYMENTRELEASED_EVENT, transactionReceipt);
        ArrayList<PaymentReleasedEventResponse> responses = new ArrayList<PaymentReleasedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            PaymentReleasedEventResponse typedResponse = new PaymentReleasedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.bookingId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.owner = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static PaymentReleasedEventResponse getPaymentReleasedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(PAYMENTRELEASED_EVENT, log);
        PaymentReleasedEventResponse typedResponse = new PaymentReleasedEventResponse();
        typedResponse.log = log;
        typedResponse.bookingId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.owner = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<PaymentReleasedEventResponse> paymentReleasedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getPaymentReleasedEventFromLog(log));
    }

    public Flowable<PaymentReleasedEventResponse> paymentReleasedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(PAYMENTRELEASED_EVENT));
        return paymentReleasedEventFlowable(filter);
    }

    public static List<PlatformFeeUpdatedEventResponse> getPlatformFeeUpdatedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(PLATFORMFEEUPDATED_EVENT, transactionReceipt);
        ArrayList<PlatformFeeUpdatedEventResponse> responses = new ArrayList<PlatformFeeUpdatedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            PlatformFeeUpdatedEventResponse typedResponse = new PlatformFeeUpdatedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.oldFee = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.newFee = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static PlatformFeeUpdatedEventResponse getPlatformFeeUpdatedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(PLATFORMFEEUPDATED_EVENT, log);
        PlatformFeeUpdatedEventResponse typedResponse = new PlatformFeeUpdatedEventResponse();
        typedResponse.log = log;
        typedResponse.oldFee = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.newFee = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<PlatformFeeUpdatedEventResponse> platformFeeUpdatedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getPlatformFeeUpdatedEventFromLog(log));
    }

    public Flowable<PlatformFeeUpdatedEventResponse> platformFeeUpdatedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(PLATFORMFEEUPDATED_EVENT));
        return platformFeeUpdatedEventFlowable(filter);
    }

    public static List<PlatformWalletChangedEventResponse> getPlatformWalletChangedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(PLATFORMWALLETCHANGED_EVENT, transactionReceipt);
        ArrayList<PlatformWalletChangedEventResponse> responses = new ArrayList<PlatformWalletChangedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            PlatformWalletChangedEventResponse typedResponse = new PlatformWalletChangedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.oldWallet = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.newWallet = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static PlatformWalletChangedEventResponse getPlatformWalletChangedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(PLATFORMWALLETCHANGED_EVENT, log);
        PlatformWalletChangedEventResponse typedResponse = new PlatformWalletChangedEventResponse();
        typedResponse.log = log;
        typedResponse.oldWallet = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.newWallet = (String) eventValues.getIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<PlatformWalletChangedEventResponse> platformWalletChangedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getPlatformWalletChangedEventFromLog(log));
    }

    public Flowable<PlatformWalletChangedEventResponse> platformWalletChangedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(PLATFORMWALLETCHANGED_EVENT));
        return platformWalletChangedEventFlowable(filter);
    }

    public static List<PropertyOwnerSetEventResponse> getPropertyOwnerSetEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(PROPERTYOWNERSET_EVENT, transactionReceipt);
        ArrayList<PropertyOwnerSetEventResponse> responses = new ArrayList<PropertyOwnerSetEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            PropertyOwnerSetEventResponse typedResponse = new PropertyOwnerSetEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.propertyId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.owner = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static PropertyOwnerSetEventResponse getPropertyOwnerSetEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(PROPERTYOWNERSET_EVENT, log);
        PropertyOwnerSetEventResponse typedResponse = new PropertyOwnerSetEventResponse();
        typedResponse.log = log;
        typedResponse.propertyId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.owner = (String) eventValues.getIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<PropertyOwnerSetEventResponse> propertyOwnerSetEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getPropertyOwnerSetEventFromLog(log));
    }

    public Flowable<PropertyOwnerSetEventResponse> propertyOwnerSetEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(PROPERTYOWNERSET_EVENT));
        return propertyOwnerSetEventFlowable(filter);
    }

    public static List<RatingAddedEventResponse> getRatingAddedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(RATINGADDED_EVENT, transactionReceipt);
        ArrayList<RatingAddedEventResponse> responses = new ArrayList<RatingAddedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            RatingAddedEventResponse typedResponse = new RatingAddedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.propertyId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.tenant = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.stars = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static RatingAddedEventResponse getRatingAddedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(RATINGADDED_EVENT, log);
        RatingAddedEventResponse typedResponse = new RatingAddedEventResponse();
        typedResponse.log = log;
        typedResponse.propertyId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.tenant = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.stars = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<RatingAddedEventResponse> ratingAddedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getRatingAddedEventFromLog(log));
    }

    public Flowable<RatingAddedEventResponse> ratingAddedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(RATINGADDED_EVENT));
        return ratingAddedEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> addRating(BigInteger _bookingId, BigInteger _stars) {
        final Function function = new Function(
                FUNC_ADDRATING, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_bookingId), 
                new org.web3j.abi.datatypes.generated.Uint8(_stars)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> bookingCounter() {
        final Function function = new Function(FUNC_BOOKINGCOUNTER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<Tuple12<BigInteger, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>> bookings(BigInteger param0) {
        final Function function = new Function(FUNC_BOOKINGS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint8>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple12<BigInteger, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>>(function,
                new Callable<Tuple12<BigInteger, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>>() {
                    @Override
                    public Tuple12<BigInteger, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple12<BigInteger, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>(
                                (BigInteger) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                (BigInteger) results.get(2).getValue(), 
                                (BigInteger) results.get(3).getValue(), 
                                (BigInteger) results.get(4).getValue(), 
                                (BigInteger) results.get(5).getValue(), 
                                (BigInteger) results.get(6).getValue(), 
                                (BigInteger) results.get(7).getValue(), 
                                (BigInteger) results.get(8).getValue(), 
                                (BigInteger) results.get(9).getValue(), 
                                (BigInteger) results.get(10).getValue(), 
                                (BigInteger) results.get(11).getValue());
                    }
                });
    }

    public RemoteFunctionCall<TransactionReceipt> cancelBooking(BigInteger _bookingId) {
        final Function function = new Function(
                FUNC_CANCELBOOKING, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_bookingId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> checkIn(BigInteger _bookingId) {
        final Function function = new Function(
                FUNC_CHECKIN, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_bookingId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> checkOut(BigInteger _bookingId) {
        final Function function = new Function(
                FUNC_CHECKOUT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_bookingId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> createBooking(BigInteger _propertyId, BigInteger _startDate, BigInteger _endDate, BigInteger _rentalAmount, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_CREATEBOOKING, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_propertyId), 
                new org.web3j.abi.datatypes.generated.Uint256(_startDate), 
                new org.web3j.abi.datatypes.generated.Uint256(_endDate), 
                new org.web3j.abi.datatypes.generated.Uint256(_rentalAmount)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<BigInteger> getAverageRating(BigInteger _propertyId) {
        final Function function = new Function(FUNC_GETAVERAGERATING, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_propertyId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<Booking> getBooking(BigInteger _bookingId) {
        final Function function = new Function(FUNC_GETBOOKING, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_bookingId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Booking>() {}));
        return executeRemoteCallSingleValueReturn(function, Booking.class);
    }

    public RemoteFunctionCall<BigInteger> getPendingWithdrawal(String _address) {
        final Function function = new Function(FUNC_GETPENDINGWITHDRAWAL, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _address)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<List> getPropertyBookings(BigInteger _propertyId) {
        final Function function = new Function(FUNC_GETPROPERTYBOOKINGS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_propertyId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Uint256>>() {}));
        return new RemoteFunctionCall<List>(function,
                new Callable<List>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    public RemoteFunctionCall<String> getPropertyOwner(BigInteger _propertyId) {
        final Function function = new Function(FUNC_GETPROPERTYOWNER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_propertyId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<List> getPropertyRatings(BigInteger _propertyId) {
        final Function function = new Function(FUNC_GETPROPERTYRATINGS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_propertyId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Rating>>() {}));
        return new RemoteFunctionCall<List>(function,
                new Callable<List>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    public RemoteFunctionCall<List> getTenantBookings(String _tenant) {
        final Function function = new Function(FUNC_GETTENANTBOOKINGS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _tenant)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Uint256>>() {}));
        return new RemoteFunctionCall<List>(function,
                new Callable<List>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    public RemoteFunctionCall<Boolean> isPropertyAvailable(BigInteger _propertyId, BigInteger _startDate, BigInteger _endDate) {
        final Function function = new Function(FUNC_ISPROPERTYAVAILABLE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_propertyId), 
                new org.web3j.abi.datatypes.generated.Uint256(_startDate), 
                new org.web3j.abi.datatypes.generated.Uint256(_endDate)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<BigInteger> pendingWithdrawals(String param0) {
        final Function function = new Function(FUNC_PENDINGWITHDRAWALS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<BigInteger> platformFeePercentage() {
        final Function function = new Function(FUNC_PLATFORMFEEPERCENTAGE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<String> platformWallet() {
        final Function function = new Function(FUNC_PLATFORMWALLET, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<BigInteger> propertyBookings(BigInteger param0, BigInteger param1) {
        final Function function = new Function(FUNC_PROPERTYBOOKINGS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0), 
                new org.web3j.abi.datatypes.generated.Uint256(param1)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<String> propertyOwners(BigInteger param0) {
        final Function function = new Function(FUNC_PROPERTYOWNERS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<Tuple4<String, BigInteger, BigInteger, BigInteger>> propertyRatings(BigInteger param0, BigInteger param1) {
        final Function function = new Function(FUNC_PROPERTYRATINGS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0), 
                new org.web3j.abi.datatypes.generated.Uint256(param1)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint8>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple4<String, BigInteger, BigInteger, BigInteger>>(function,
                new Callable<Tuple4<String, BigInteger, BigInteger, BigInteger>>() {
                    @Override
                    public Tuple4<String, BigInteger, BigInteger, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple4<String, BigInteger, BigInteger, BigInteger>(
                                (String) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue(), 
                                (BigInteger) results.get(2).getValue(), 
                                (BigInteger) results.get(3).getValue());
                    }
                });
    }

    public RemoteFunctionCall<BigInteger> ratingCount(BigInteger param0) {
        final Function function = new Function(FUNC_RATINGCOUNT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<BigInteger> ratingSum(BigInteger param0) {
        final Function function = new Function(FUNC_RATINGSUM, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> releaseFunds(BigInteger _bookingId) {
        final Function function = new Function(
                FUNC_RELEASEFUNDS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_bookingId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> renounceOwnership() {
        final Function function = new Function(
                FUNC_RENOUNCEOWNERSHIP, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setPlatformFee(BigInteger _newFee) {
        final Function function = new Function(
                FUNC_SETPLATFORMFEE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_newFee)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setPlatformWallet(String _newWallet) {
        final Function function = new Function(
                FUNC_SETPLATFORMWALLET, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _newWallet)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setPropertyOwner(BigInteger _propertyId, String _owner) {
        final Function function = new Function(
                FUNC_SETPROPERTYOWNER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_propertyId), 
                new org.web3j.abi.datatypes.Address(160, _owner)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> tenantBookings(String param0, BigInteger param1) {
        final Function function = new Function(FUNC_TENANTBOOKINGS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, param0), 
                new org.web3j.abi.datatypes.generated.Uint256(param1)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> transferOwnership(String newOwner) {
        final Function function = new Function(
                FUNC_TRANSFEROWNERSHIP, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, newOwner)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> withdraw() {
        final Function function = new Function(
                FUNC_WITHDRAW, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static RentalPlatform load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new RentalPlatform(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static RentalPlatform load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new RentalPlatform(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static RentalPlatform load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new RentalPlatform(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static RentalPlatform load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new RentalPlatform(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<RentalPlatform> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider, String _platformWallet) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _platformWallet)));
        return deployRemoteCall(RentalPlatform.class, web3j, credentials, contractGasProvider, BINARY, encodedConstructor);
    }

    public static RemoteCall<RentalPlatform> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider, String _platformWallet) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _platformWallet)));
        return deployRemoteCall(RentalPlatform.class, web3j, transactionManager, contractGasProvider, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<RentalPlatform> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, String _platformWallet) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _platformWallet)));
        return deployRemoteCall(RentalPlatform.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<RentalPlatform> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, String _platformWallet) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _platformWallet)));
        return deployRemoteCall(RentalPlatform.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static class Booking extends StaticStruct {
        public BigInteger bookingId;

        public String tenant;

        public BigInteger propertyId;

        public BigInteger startDate;

        public BigInteger endDate;

        public BigInteger rentalAmount;

        public BigInteger platformFee;

        public BigInteger totalAmount;

        public BigInteger status;

        public BigInteger createdAt;

        public BigInteger checkInTime;

        public BigInteger checkOutTime;

        public Booking(BigInteger bookingId, String tenant, BigInteger propertyId, BigInteger startDate, BigInteger endDate, BigInteger rentalAmount, BigInteger platformFee, BigInteger totalAmount, BigInteger status, BigInteger createdAt, BigInteger checkInTime, BigInteger checkOutTime) {
            super(new org.web3j.abi.datatypes.generated.Uint256(bookingId), 
                    new org.web3j.abi.datatypes.Address(160, tenant), 
                    new org.web3j.abi.datatypes.generated.Uint256(propertyId), 
                    new org.web3j.abi.datatypes.generated.Uint256(startDate), 
                    new org.web3j.abi.datatypes.generated.Uint256(endDate), 
                    new org.web3j.abi.datatypes.generated.Uint256(rentalAmount), 
                    new org.web3j.abi.datatypes.generated.Uint256(platformFee), 
                    new org.web3j.abi.datatypes.generated.Uint256(totalAmount), 
                    new org.web3j.abi.datatypes.generated.Uint8(status), 
                    new org.web3j.abi.datatypes.generated.Uint256(createdAt), 
                    new org.web3j.abi.datatypes.generated.Uint256(checkInTime), 
                    new org.web3j.abi.datatypes.generated.Uint256(checkOutTime));
            this.bookingId = bookingId;
            this.tenant = tenant;
            this.propertyId = propertyId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.rentalAmount = rentalAmount;
            this.platformFee = platformFee;
            this.totalAmount = totalAmount;
            this.status = status;
            this.createdAt = createdAt;
            this.checkInTime = checkInTime;
            this.checkOutTime = checkOutTime;
        }

        public Booking(Uint256 bookingId, Address tenant, Uint256 propertyId, Uint256 startDate, Uint256 endDate, Uint256 rentalAmount, Uint256 platformFee, Uint256 totalAmount, Uint8 status, Uint256 createdAt, Uint256 checkInTime, Uint256 checkOutTime) {
            super(bookingId, tenant, propertyId, startDate, endDate, rentalAmount, platformFee, totalAmount, status, createdAt, checkInTime, checkOutTime);
            this.bookingId = bookingId.getValue();
            this.tenant = tenant.getValue();
            this.propertyId = propertyId.getValue();
            this.startDate = startDate.getValue();
            this.endDate = endDate.getValue();
            this.rentalAmount = rentalAmount.getValue();
            this.platformFee = platformFee.getValue();
            this.totalAmount = totalAmount.getValue();
            this.status = status.getValue();
            this.createdAt = createdAt.getValue();
            this.checkInTime = checkInTime.getValue();
            this.checkOutTime = checkOutTime.getValue();
        }
    }

    public static class Rating extends StaticStruct {
        public String tenant;

        public BigInteger propertyId;

        public BigInteger stars;

        public BigInteger timestamp;

        public Rating(String tenant, BigInteger propertyId, BigInteger stars, BigInteger timestamp) {
            super(new org.web3j.abi.datatypes.Address(160, tenant), 
                    new org.web3j.abi.datatypes.generated.Uint256(propertyId), 
                    new org.web3j.abi.datatypes.generated.Uint8(stars), 
                    new org.web3j.abi.datatypes.generated.Uint256(timestamp));
            this.tenant = tenant;
            this.propertyId = propertyId;
            this.stars = stars;
            this.timestamp = timestamp;
        }

        public Rating(Address tenant, Uint256 propertyId, Uint8 stars, Uint256 timestamp) {
            super(tenant, propertyId, stars, timestamp);
            this.tenant = tenant.getValue();
            this.propertyId = propertyId.getValue();
            this.stars = stars.getValue();
            this.timestamp = timestamp.getValue();
        }
    }

    public static class BookingCancelledEventResponse extends BaseEventResponse {
        public BigInteger bookingId;

        public BigInteger refundAmount;
    }

    public static class BookingCompletedEventResponse extends BaseEventResponse {
        public BigInteger bookingId;
    }

    public static class BookingConfirmedEventResponse extends BaseEventResponse {
        public BigInteger bookingId;
    }

    public static class BookingCreatedEventResponse extends BaseEventResponse {
        public BigInteger bookingId;

        public String tenant;

        public BigInteger propertyId;

        public BigInteger totalAmount;
    }

    public static class CheckInCompletedEventResponse extends BaseEventResponse {
        public BigInteger bookingId;

        public BigInteger timestamp;
    }

    public static class CheckOutCompletedEventResponse extends BaseEventResponse {
        public BigInteger bookingId;

        public BigInteger timestamp;
    }

    public static class OwnershipTransferredEventResponse extends BaseEventResponse {
        public String previousOwner;

        public String newOwner;
    }

    public static class PaymentReleasedEventResponse extends BaseEventResponse {
        public BigInteger bookingId;

        public String owner;

        public BigInteger amount;
    }

    public static class PlatformFeeUpdatedEventResponse extends BaseEventResponse {
        public BigInteger oldFee;

        public BigInteger newFee;
    }

    public static class PlatformWalletChangedEventResponse extends BaseEventResponse {
        public String oldWallet;

        public String newWallet;
    }

    public static class PropertyOwnerSetEventResponse extends BaseEventResponse {
        public BigInteger propertyId;

        public String owner;
    }

    public static class RatingAddedEventResponse extends BaseEventResponse {
        public BigInteger propertyId;

        public String tenant;

        public BigInteger stars;
    }
}
