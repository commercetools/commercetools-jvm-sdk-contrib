package io.sphere.sdk.contrib.products.commands;

import com.commercetools.sdk.contrib.SphereClientRule;
import io.sphere.sdk.models.LocalizedString;
import io.sphere.sdk.products.*;
import io.sphere.sdk.products.commands.ProductCreateCommand;
import io.sphere.sdk.producttypes.ProductType;
import io.sphere.sdk.producttypes.ProductTypeDraft;
import io.sphere.sdk.producttypes.commands.ProductTypeCreateCommand;
import io.sphere.sdk.producttypes.queries.ProductTypeQuery;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.File;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class ProductImageUploadCommandIntegrationTest {
    @ClassRule
    public static SphereClientRule client = new SphereClientRule();
    private static final String SLUG = ProductImageUploadCommandIntegrationTest.class.getSimpleName().substring(0, 36);
    private static final ProductVariantDraft MASTER_VARIANT = ProductVariantDraftBuilder.of().build();

    private Product product;

    @Before
    public void setUp() throws Exception {
        product = createProduct();
    }

    @Test
    public void upload() {
        final ByIdVariantIdentifier identifier = product.getMasterData().getStaged().getMasterVariant().getIdentifier();
        final ProductImageUploadCommand cmd = ProductImageUploadCommand
                .ofVariantId(new File("src/test/resources/ct_logo_farbe.gif"), identifier)
                .withFilename("logo.gif")
                .withStaged(true);
        final Product product = client.executeBlocking(cmd);
        final Image image = product.getMasterData().getStaged().getMasterVariant().getImages().get(0);
        assertThat(image.getDimensions().getHeight()).isEqualTo(102);
        assertThat(image.getDimensions().getWidth()).isEqualTo(460);
        assertThat(image.getUrl()).contains("logo");
    }

    private ProductType productType() {
        return client.executeBlocking(ProductTypeQuery.of().byName(SLUG))
                .head()
                .orElseGet(() -> {
                    final ProductTypeDraft productTypeDraft = ProductTypeDraft.of(SLUG, SLUG, "", asList());
                    return client.executeBlocking(ProductTypeCreateCommand.of(productTypeDraft));
                });
    }

    private Product createProduct() {
        final ProductDraft productDraft =
                ProductDraftBuilder.of(productType(), LocalizedString.ofEnglish("test"),
                        LocalizedString.ofEnglish(RandomStringUtils.randomAlphanumeric(32)), MASTER_VARIANT)
                        .build();
        return client.executeBlocking(ProductCreateCommand.of(productDraft));
    }
}