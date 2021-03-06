package nl.hackathon.ethereum;

import static org.adridadou.ethereum.ethj.provider.EthereumJConfigs.ropsten;

import java.util.concurrent.ExecutionException;

import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.ethj.provider.EthereumFacadeProvider;
import org.adridadou.ethereum.ethj.provider.PrivateEthereumFacadeProvider;
import org.adridadou.ethereum.ethj.provider.PrivateNetworkConfig;
import org.adridadou.ethereum.keystore.AccountProvider;
import org.adridadou.ethereum.values.EthAccount;
import org.adridadou.ethereum.values.EthAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class NetManager {

    @Value("${main.pkey}")
    private String mainPKey;
    @Value("${musician.address}")
    private String musicianAddress;


    @Bean
    @Profile({ "integration" })
    public EthereumFacade ethereumRopstenNetFacade() throws ExecutionException, InterruptedException {
        EthereumFacade facade = EthereumFacadeProvider.forNetwork(ropsten().fastSync(true)).create();

        return facade;
    }

    @Bean
    @Profile({ "bootstrap" })
    public EthereumFacade ethereumRopstenNetFacadeWithBootstrap() throws ExecutionException, InterruptedException {
        EthereumFacade facade = EthereumFacadeProvider.forNetwork(ropsten().fastSync(true)).create();
        bootstrapContracts(facade);
        return facade;
    }

    @Bean
    @Profile({ "development" })
    public EthereumFacade ethereumPrivateNetFacade() throws ExecutionException, InterruptedException {
        PrivateEthereumFacadeProvider privateNetwork = new PrivateEthereumFacadeProvider();
        EthereumFacade facade = privateNetwork.create(PrivateNetworkConfig.config());

        bootstrapContracts(facade);

        return facade;
    }

    private void bootstrapContracts(EthereumFacade facade) throws ExecutionException, InterruptedException {
        EthAccount mainAccount = AccountProvider.fromPrivateKey(mainPKey);

        ContractPublisher contractPublisher = new ContractPublisher();
        EthAddress ratingAddress = contractPublisher.publishRatingContract(facade, mainAccount);
        EthAddress walletAddress = contractPublisher.publishWalletContract(facade, mainAccount, ratingAddress, EthAddress.of(musicianAddress));

    }

}
