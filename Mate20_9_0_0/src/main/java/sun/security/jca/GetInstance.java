package sun.security.jca;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Provider.Service;
import java.util.List;

public class GetInstance {

    public static final class Instance {
        public final Object impl;
        public final Provider provider;

        private Instance(Provider provider, Object impl) {
            this.provider = provider;
            this.impl = impl;
        }

        public Object[] toArray() {
            return new Object[]{this.impl, this.provider};
        }
    }

    private GetInstance() {
    }

    public static Service getService(String type, String algorithm) throws NoSuchAlgorithmException {
        Service s = Providers.getProviderList().getService(type, algorithm);
        if (s != null) {
            return s;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(algorithm);
        stringBuilder.append(" ");
        stringBuilder.append(type);
        stringBuilder.append(" not available");
        throw new NoSuchAlgorithmException(stringBuilder.toString());
    }

    public static Service getService(String type, String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        if (provider == null || provider.length() == 0) {
            throw new IllegalArgumentException("missing provider");
        }
        Provider p = Providers.getProviderList().getProvider(provider);
        if (p != null) {
            Service s = p.getService(type, algorithm);
            if (s != null) {
                return s;
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("no such algorithm: ");
            stringBuilder.append(algorithm);
            stringBuilder.append(" for provider ");
            stringBuilder.append(provider);
            throw new NoSuchAlgorithmException(stringBuilder.toString());
        }
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("no such provider: ");
        stringBuilder2.append(provider);
        throw new NoSuchProviderException(stringBuilder2.toString());
    }

    public static Service getService(String type, String algorithm, Provider provider) throws NoSuchAlgorithmException {
        if (provider != null) {
            Service s = provider.getService(type, algorithm);
            if (s != null) {
                return s;
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("no such algorithm: ");
            stringBuilder.append(algorithm);
            stringBuilder.append(" for provider ");
            stringBuilder.append(provider.getName());
            throw new NoSuchAlgorithmException(stringBuilder.toString());
        }
        throw new IllegalArgumentException("missing provider");
    }

    public static List<Service> getServices(String type, String algorithm) {
        return Providers.getProviderList().getServices(type, algorithm);
    }

    @Deprecated
    public static List<Service> getServices(String type, List<String> algorithms) {
        return Providers.getProviderList().getServices(type, (List) algorithms);
    }

    public static List<Service> getServices(List<ServiceId> ids) {
        return Providers.getProviderList().getServices(ids);
    }

    public static Instance getInstance(String type, Class<?> clazz, String algorithm) throws NoSuchAlgorithmException {
        ProviderList list = Providers.getProviderList();
        Service firstService = list.getService(type, algorithm);
        if (firstService != null) {
            try {
                return getInstance(firstService, clazz);
            } catch (NoSuchAlgorithmException e) {
                NoSuchAlgorithmException failure = e;
                for (Service s : list.getServices(type, algorithm)) {
                    if (s != firstService) {
                        try {
                            return getInstance(s, clazz);
                        } catch (NoSuchAlgorithmException e2) {
                            failure = e2;
                        }
                    }
                }
                throw failure;
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(algorithm);
        stringBuilder.append(" ");
        stringBuilder.append(type);
        stringBuilder.append(" not available");
        throw new NoSuchAlgorithmException(stringBuilder.toString());
    }

    public static Instance getInstance(String type, Class<?> clazz, String algorithm, Object param) throws NoSuchAlgorithmException {
        NoSuchAlgorithmException failure = null;
        for (Service s : getServices(type, algorithm)) {
            try {
                return getInstance(s, (Class) clazz, param);
            } catch (NoSuchAlgorithmException e) {
                failure = e;
            }
        }
        if (failure != null) {
            throw failure;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(algorithm);
        stringBuilder.append(" ");
        stringBuilder.append(type);
        stringBuilder.append(" not available");
        throw new NoSuchAlgorithmException(stringBuilder.toString());
    }

    public static Instance getInstance(String type, Class<?> clazz, String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        return getInstance(getService(type, algorithm, provider), clazz);
    }

    public static Instance getInstance(String type, Class<?> clazz, String algorithm, Object param, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        return getInstance(getService(type, algorithm, provider), (Class) clazz, param);
    }

    public static Instance getInstance(String type, Class<?> clazz, String algorithm, Provider provider) throws NoSuchAlgorithmException {
        return getInstance(getService(type, algorithm, provider), clazz);
    }

    public static Instance getInstance(String type, Class<?> clazz, String algorithm, Object param, Provider provider) throws NoSuchAlgorithmException {
        return getInstance(getService(type, algorithm, provider), (Class) clazz, param);
    }

    public static Instance getInstance(Service s, Class<?> clazz) throws NoSuchAlgorithmException {
        Object instance = s.newInstance(null);
        checkSuperClass(s, instance.getClass(), clazz);
        return new Instance(s.getProvider(), instance);
    }

    public static Instance getInstance(Service s, Class<?> clazz, Object param) throws NoSuchAlgorithmException {
        Object instance = s.newInstance(param);
        checkSuperClass(s, instance.getClass(), clazz);
        return new Instance(s.getProvider(), instance);
    }

    public static void checkSuperClass(Service s, Class<?> subClass, Class<?> superClass) throws NoSuchAlgorithmException {
        if (superClass != null && !superClass.isAssignableFrom(subClass)) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("class configured for ");
            stringBuilder.append(s.getType());
            stringBuilder.append(": ");
            stringBuilder.append(s.getClassName());
            stringBuilder.append(" not a ");
            stringBuilder.append(s.getType());
            throw new NoSuchAlgorithmException(stringBuilder.toString());
        }
    }
}
